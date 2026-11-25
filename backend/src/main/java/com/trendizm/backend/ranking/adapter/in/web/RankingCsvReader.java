package com.trendizm.backend.ranking.adapter.in.web;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.global.error.ErrorResponse;
import com.trendizm.backend.ranking.application.dto.RankingImportRow;
import com.trendizm.backend.ranking.application.exception.RankingErrorCode;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.MappingIterator;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.csv.CsvMapper;
import tools.jackson.dataformat.csv.CsvReadFeature;

/**
 * 순위 CSV 본문을 줄마다 RankingImportRow로 바꾼다(CSV → JSON과 같은 구조). 값이 맞는지는 서비스가 검사한다.
 * - 본문은 UTF-8로만 읽고 맨 앞의 BOM은 지운다.
 * - 헤더는 열 이름으로 찾으므로 열 순서는 상관없고, 없는 열이 있으면 거부한다. count 같은 다른 열은 쓰지 않는다.
 * - 빈 줄과 쉼표만 있는 줄(엑셀에서 저장하면 끝에 붙기도 한다)은 건너뛴다.
 * - 따옴표가 닫히지 않는 등 CSV 문법이 틀리면 그 줄에서 읽기를 멈추고 줄 번호를 알린다.
 * - 열 개수가 헤더와 다르거나 JSON 배열 칸이 틀린 줄은 끝까지 읽어 모두 함께 알린다.
 */
final class RankingCsvReader {

    /** 본문 최대 크기. 관련 게시물 제목(최대 255자)이 줄마다 10개씩 들어가도 넉넉한 크기 */
    static final int MAX_BYTES = 5 * 1024 * 1024;

    static final List<String> COLUMNS = List.of("platform", "category", "ranking", "keyword", "related_posts",
            "related_posts_author", "related_posts_title", "collected_at", "started_at");

    // 빈 줄도 한 행으로 읽어야 행의 시작 줄 번호가 맞는다. 빈 줄은 아래에서 직접 건너뛴다
    private static final CsvMapper CSV = CsvMapper.builder().enable(CsvReadFeature.WRAP_AS_ARRAY).build();
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {
    };

    private RankingCsvReader() {
    }

    static List<RankingImportRow> read(byte[] body) {
        if (body.length > MAX_BYTES) {
            throw new BusinessException(RankingErrorCode.RANKING_CSV_TOO_LARGE);
        }
        String text = decode(body);

        List<RankingImportRow> rows = new ArrayList<>();
        List<ErrorResponse.FieldError> errors = new ArrayList<>();
        Map<String, Integer> columns = null;
        int width = 0;
        try (MappingIterator<String[]> iterator = CSV.readerFor(String[].class).readValues(text)) {
            while (true) {
                int line = iterator.currentLocation().getLineNr(); // 다음 행이 시작하는 줄
                if (!iterator.hasNextValue()) {
                    break;
                }
                String[] cells = iterator.nextValue();
                if (Arrays.stream(cells).allMatch(String::isBlank)) {
                    continue;
                }
                if (columns == null) {
                    columns = header(cells, line);
                    width = cells.length;
                    continue;
                }
                if (cells.length != width) {
                    errors.add(error(line, "열이 " + cells.length + "개입니다. 헤더는 " + width
                            + "개입니다. 쉼표가 들어간 칸을 따옴표로 감쌌는지 확인해 주세요."));
                    continue;
                }
                rows.add(new RankingImportRow(line,
                        cells[columns.get("platform")],
                        cells[columns.get("category")],
                        cells[columns.get("ranking")],
                        cells[columns.get("keyword")],
                        jsonArray(cells[columns.get("related_posts")], "related_posts", line, errors),
                        jsonArray(cells[columns.get("related_posts_author")], "related_posts_author", line, errors),
                        jsonArray(cells[columns.get("related_posts_title")], "related_posts_title", line, errors),
                        cells[columns.get("collected_at")],
                        cells[columns.get("started_at")]));
            }
        } catch (JacksonException e) {
            int line = e.getLocation() == null ? -1 : e.getLocation().getLineNr();
            errors.add(error(line, "CSV 형식이 올바르지 않습니다. 따옴표가 닫혔는지, 칸 안의 따옴표를 \"\"로 썼는지 확인해 주세요."));
        }
        if (columns == null && errors.isEmpty()) {
            errors.add(new ErrorResponse.FieldError("CSV", "헤더 줄이 없습니다."));
        }
        if (!errors.isEmpty()) {
            throw invalid(errors);
        }
        return rows;
    }

    /** 엄격한 UTF-8로 읽는다. 엑셀 기본값(CP949)으로 저장한 파일은 한글이 깨지므로 거부한다 */
    private static String decode(byte[] body) {
        try {
            String text = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(body))
                    .toString();
            return text.startsWith("\uFEFF") ? text.substring(1) : text;
        } catch (CharacterCodingException e) {
            throw new BusinessException(RankingErrorCode.RANKING_CSV_INVALID,
                    List.of(new ErrorResponse.FieldError("CSV", "UTF-8로 저장된 CSV가 아닙니다. 엑셀에서는 'CSV UTF-8' 형식으로 저장해 주세요.")));
        }
    }

    private static Map<String, Integer> header(String[] cells, int line) {
        Map<String, Integer> columns = new HashMap<>();
        for (int i = 0; i < cells.length; i++) {
            if (columns.putIfAbsent(cells[i].strip(), i) != null) {
                throw invalid(List.of(error(line, "헤더에 " + cells[i].strip() + " 열이 두 번 있습니다.")));
            }
        }
        List<String> missing = COLUMNS.stream().filter(column -> !columns.containsKey(column)).toList();
        if (!missing.isEmpty()) {
            throw invalid(List.of(error(line, "헤더에 없는 열이 있습니다: " + String.join(", ", missing))));
        }
        return columns;
    }

    /** JSON 문자열 배열 칸. 비어 있으면 빈 목록이고, 배열이 아니면 오류로 모은다 */
    private static List<String> jsonArray(String value, String column, int line, List<ErrorResponse.FieldError> errors) {
        String text = value.strip();
        if (text.isEmpty()) {
            return List.of();
        }
        try {
            List<String> values = JSON.readValue(text, STRING_LIST);
            if (values != null) {
                return values;
            }
        } catch (JacksonException ignored) {
            // 아래에서 알린다
        }
        errors.add(error(line, column + ": JSON 문자열 배열이 아닙니다. 예: [\"Da0E9_oBZM-\", \"DZrXQRiSv6q\"]"));
        return List.of();
    }

    private static ErrorResponse.FieldError error(int line, String message) {
        return new ErrorResponse.FieldError(line < 1 ? "CSV" : line + "번째 줄", message);
    }

    private static BusinessException invalid(List<ErrorResponse.FieldError> errors) {
        return new BusinessException(RankingErrorCode.RANKING_CSV_INVALID, errors);
    }
}
