package com.trendizm.backend.ranking.application.port.in;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.global.error.ErrorResponse;
import com.trendizm.backend.ranking.application.dto.RankingImportResult.MergedKeyword;
import com.trendizm.backend.ranking.application.dto.RankingImportRow;
import com.trendizm.backend.ranking.application.exception.RankingErrorCode;
import com.trendizm.backend.ranking.domain.entity.TrendRankingLink;
import com.trendizm.backend.ranking.domain.enums.Platform;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 순위 CSV 업로드(POST /api/admin/rankings)에서 DB를 쓰지 않는 부분: 줄마다 값 검사와 카테고리 안의 키워드 정리.
 * 검사는 틀린 줄을 모두 모아 RANKING_CSV_INVALID 하나로 알린다(errors에 "N번째 줄"과 이유).
 */
final class RankingImportRules {

    /** 순위 키워드 최대 길이 (trend_rankings.keyword varchar(255)) */
    static final int KEYWORD_MAX_LENGTH = 255;

    /** 대소문자만 다른 키워드를 합칠 때, 영문자가 이 수 이하면 대문자로, 넘으면 소문자로 저장한다 */
    static final int UPPERCASE_MAX_LETTERS = 4;

    private RankingImportRules() {
    }

    /** 검사를 통과한 줄. rank가 null이면 순위 칸이 빈 줄이다(저장하지 않고 skipped로 알린다) */
    record ParsedRow(int line, Platform platform, String category, Integer rank, String keyword,
                     List<ParsedLink> links, LocalDateTime collectedAt, LocalDateTime startedAt) {
    }

    /** sortOrder는 links 안의 순서(0부터)다 */
    record ParsedLink(String url, String title) {
    }

    /** 카테고리 안에서 정리한 키워드. rank는 1부터 다시 매긴 순위, keyword는 저장할 형태다 */
    record RankedKeyword(ParsedRow row, int rank, String keyword) {
    }

    record RankedSnapshot(List<RankedKeyword> rankings, List<MergedKeyword> merged) {
    }

    /**
     * 줄마다 값을 검사한다. 틀린 줄이 하나라도 있으면 모두 모아 RANKING_CSV_INVALID를 던진다.
     * collected_at·started_at은 한 번 수집한 결과라서 파일 안의 모든 줄이 같아야 한다.
     */
    static List<ParsedRow> parse(List<RankingImportRow> rows) {
        if (rows.isEmpty()) {
            throw invalid(List.of(new ErrorResponse.FieldError("CSV", "헤더 아래에 순위 줄이 없습니다.")));
        }
        List<ErrorResponse.FieldError> errors = new ArrayList<>();
        List<ParsedRow> parsed = new ArrayList<>();
        for (RankingImportRow row : rows) {
            parseRow(row, errors).ifPresent(parsed::add);
        }
        if (errors.isEmpty()) {
            ParsedRow first = parsed.getFirst();
            for (ParsedRow row : parsed) {
                if (!row.collectedAt().equals(first.collectedAt())) {
                    errors.add(error(row.line(), "collected_at이 " + first.line() + "번째 줄(" + first.collectedAt()
                            + ")과 다릅니다. 한 파일의 모든 줄은 같은 수집 시각이어야 합니다."));
                }
                if (!Objects.equals(row.startedAt(), first.startedAt())) {
                    errors.add(error(row.line(), "started_at이 " + first.line() + "번째 줄(" + first.startedAt()
                            + ")과 다릅니다. 한 파일의 모든 줄은 같은 시작일이어야 합니다."));
                }
            }
        }
        if (!errors.isEmpty()) {
            throw invalid(errors);
        }
        return parsed;
    }

    /**
     * 한 스냅샷(같은 플랫폼·카테고리)의 줄을 순위 순으로 정리한다.
     * 1. 순위가 높은 줄부터, 순위가 같으면 위쪽 줄부터 놓는다.
     * 2. 대소문자를 무시하고 같은 키워드는 첫 줄만 남긴다. 대소문자만 다르면 영문자 수로 형태를 정하고(caseMergedKeyword),
     *    완전히 같으면 그대로 둔다. 중복이 없는 키워드는 바꾸지 않는다.
     * 3. 남은 줄에 1부터 순위를 다시 매긴다. 지운 줄의 뒤 순위는 당겨지고, 서로 다른 키워드가 같은 순위면 위쪽 줄이 앞선다.
     */
    static RankedSnapshot rank(List<ParsedRow> rows) {
        Map<String, List<ParsedRow>> groups = new LinkedHashMap<>();
        rows.stream()
                .sorted(Comparator.comparing(ParsedRow::rank).thenComparing(ParsedRow::line))
                .forEach(row -> groups.computeIfAbsent(row.keyword().toLowerCase(Locale.ROOT), key -> new ArrayList<>())
                        .add(row));

        List<RankedKeyword> rankings = new ArrayList<>();
        List<MergedKeyword> merged = new ArrayList<>();
        for (List<ParsedRow> group : groups.values()) {
            ParsedRow kept = group.getFirst();
            String keyword = kept.keyword();
            if (group.size() > 1) {
                boolean caseVariants = group.stream().map(ParsedRow::keyword).distinct().count() > 1;
                if (caseVariants) {
                    keyword = caseMergedKeyword(keyword);
                }
                merged.add(new MergedKeyword(kept.category(), keyword, group.stream().map(ParsedRow::line).toList()));
            }
            rankings.add(new RankedKeyword(kept, rankings.size() + 1, keyword));
        }
        return new RankedSnapshot(rankings, merged);
    }

    /**
     * 대소문자만 다른 키워드를 합칠 때 저장할 형태. 영문자(A-Z, a-z)가 4개 이하면 대문자, 5개 이상이면 소문자다.
     * 예: l을가져가(영문자 1개) → L을가져가, copypaste(9개) → copypaste, Tmi(3개) → TMI
     */
    static String caseMergedKeyword(String keyword) {
        long letters = keyword.chars().filter(ch -> (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')).count();
        return letters <= UPPERCASE_MAX_LETTERS ? keyword.toUpperCase(Locale.ROOT) : keyword.toLowerCase(Locale.ROOT);
    }

    private static Optional<ParsedRow> parseRow(RankingImportRow row, List<ErrorResponse.FieldError> errors) {
        int line = row.line();
        int before = errors.size();

        Platform platform = Platform.fromSlug(strip(row.platform())).orElse(null);
        if (platform == null) {
            errors.add(error(line, "platform: 지원하지 않는 플랫폼입니다 (값: " + row.platform() + ")."));
        }
        String category = strip(row.category());
        if (category.isEmpty()) {
            errors.add(error(line, "category: 비어 있습니다."));
        }
        Integer rank = parseRank(strip(row.ranking()), line, errors);
        String keyword = strip(row.keyword());
        if (keyword.isEmpty()) {
            errors.add(error(line, "keyword: 비어 있습니다."));
        } else if (keyword.codePointCount(0, keyword.length()) > KEYWORD_MAX_LENGTH) {
            errors.add(error(line, "keyword: " + KEYWORD_MAX_LENGTH + "자를 넘습니다."));
        }
        List<ParsedLink> links = platform == null ? List.of() : parseLinks(row, platform, errors);
        LocalDateTime collectedAt = parseTime(row.collectedAt(), "collected_at", true, line, errors);
        LocalDateTime startedAt = parseTime(row.startedAt(), "started_at", false, line, errors);

        if (errors.size() > before) {
            return Optional.empty();
        }
        return Optional.of(new ParsedRow(line, platform, category, rank, keyword, links, collectedAt, startedAt));
    }

    /** 빈 칸이면 null(순위 없음). 값이 있으면 1 이상의 정수여야 한다 */
    private static Integer parseRank(String value, int line, List<ErrorResponse.FieldError> errors) {
        if (value.isEmpty()) {
            return null;
        }
        try {
            int rank = Integer.parseInt(value);
            if (rank >= 1) {
                return rank;
            }
        } catch (NumberFormatException ignored) {
            // 아래에서 같은 오류로 알린다
        }
        errors.add(error(line, "ranking: 1 이상의 정수여야 합니다 (값: " + value + ")."));
        return null;
    }

    /**
     * related_posts의 게시물 ID로 주소를 만든다. 작성자·제목 배열은 비어 있거나 related_posts와 개수가 같아야 한다.
     * 주소에 작성자가 들어가는 플랫폼(threads, tiktok)은 게시물마다 작성자가 있어야 한다.
     */
    private static List<ParsedLink> parseLinks(RankingImportRow row, Platform platform,
                                               List<ErrorResponse.FieldError> errors) {
        int line = row.line();
        List<String> ids = row.postIds();
        List<String> authors = row.postAuthors();
        List<String> titles = row.postTitles();
        boolean sizesMatch = true;
        if (!authors.isEmpty() && authors.size() != ids.size()) {
            errors.add(error(line, "related_posts_author: related_posts와 개수가 다릅니다 (related_posts " + ids.size()
                    + "개, related_posts_author " + authors.size() + "개)."));
            sizesMatch = false;
        }
        if (!titles.isEmpty() && titles.size() != ids.size()) {
            errors.add(error(line, "related_posts_title: related_posts와 개수가 다릅니다 (related_posts " + ids.size()
                    + "개, related_posts_title " + titles.size() + "개)."));
            sizesMatch = false;
        }
        if (!sizesMatch) {
            return List.of();
        }

        List<ParsedLink> links = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i) == null ? null : ids.get(i).strip();
            String author = authors.isEmpty() || authors.get(i) == null ? null : authors.get(i).strip();
            if (!Platform.isValidPostId(id)) {
                errors.add(error(line, "related_posts " + (i + 1) + "번째: 게시물 ID는 영문·숫자·_·-만 쓸 수 있습니다 (값: "
                        + id + ")."));
                continue;
            }
            if (platform.requiresAuthor() && !Platform.isValidAuthor(author)) {
                errors.add(error(line, "related_posts_author " + (i + 1) + "번째: " + platform.name()
                        + " 주소에는 작성자 계정 이름(영문·숫자·.·_)이 필요합니다 (값: " + author + ")."));
                continue;
            }
            String url = platform.postUrl(id, author);
            if (url.length() > TrendRankingLink.URL_MAX_LENGTH) {
                errors.add(error(line, "related_posts " + (i + 1) + "번째: 만든 주소가 " + TrendRankingLink.URL_MAX_LENGTH
                        + "자를 넘습니다."));
                continue;
            }
            links.add(new ParsedLink(url, titles.isEmpty() ? null : titles.get(i)));
        }
        return links;
    }

    /**
     * 한국 시각으로 읽는다. 2026-11-23 12:05, 2026-11-23 12:05:30, 2026-11-23T12:05, 2026-11-23(그날 0시)을 받는다.
     * required가 false면 빈 칸은 null이다.
     */
    private static LocalDateTime parseTime(String value, String column, boolean required, int line,
                                           List<ErrorResponse.FieldError> errors) {
        String text = strip(value);
        if (text.isEmpty()) {
            if (required) {
                errors.add(error(line, column + ": 비어 있습니다."));
            }
            return null;
        }
        try {
            return text.length() == 10 ? LocalDate.parse(text).atStartOfDay() : LocalDateTime.parse(text.replace(' ', 'T'));
        } catch (DateTimeParseException e) {
            errors.add(error(line, column + ": 날짜·시각 형식이 아닙니다. 예: 2026-11-23 12:05 (값: " + text + ")."));
            return null;
        }
    }

    private static String strip(String value) {
        return value == null ? "" : value.strip();
    }

    private static ErrorResponse.FieldError error(int line, String message) {
        return new ErrorResponse.FieldError(line + "번째 줄", message);
    }

    private static BusinessException invalid(List<ErrorResponse.FieldError> errors) {
        return new BusinessException(RankingErrorCode.RANKING_CSV_INVALID, errors);
    }
}
