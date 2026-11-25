package com.trendizm.backend.ranking.application.port.in;

import com.trendizm.backend.ranking.application.port.out.RankingSummaryGenerator.Answer;
import com.trendizm.backend.ranking.application.port.out.RankingSummaryGenerator.Target;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/** AI 요약을 저장해도 되는지 확인한다. 요약은 로그인하지 않은 사람에게도 보이므로 확인되지 않으면 저장하지 않는다 */
final class RankingSummaryRules {

    /** 저장하는 요약 최대 길이. 지시(300자)보다 길게 써도 여기까지는 받는다 */
    static final int MAX_LENGTH = 500;

    /** 게시물 글에 섞인 링크·계정 이름이 요약으로 옮겨진 것 */
    private static final Pattern LINK_OR_ACCOUNT = Pattern.compile("https?://|www\\.|@[A-Za-z0-9_.]");

    private static final Pattern SPACES = Pattern.compile("\\s+");

    private RankingSummaryRules() {
    }

    /** 앞뒤 공백을 지우고 줄바꿈·연속 공백을 공백 하나로 바꾼다. null이면 빈 문자열 */
    static String oneLine(String text) {
        return text == null ? "" : SPACES.matcher(text.strip()).replaceAll(" ");
    }

    /** 저장하면 안 되는 이유. 저장해도 되면 empty */
    static Optional<String> rejectReason(Target target, Answer answer) {
        String summary = oneLine(answer.summary());
        if (summary.isEmpty()) {
            return Optional.of("게시물 글만으로는 무엇인지 알 수 없습니다.");
        }
        if (summary.codePointCount(0, summary.length()) > MAX_LENGTH) {
            return Optional.of("요약이 " + MAX_LENGTH + "자를 넘습니다.");
        }
        if (LINK_OR_ACCOUNT.matcher(summary).find()) {
            return Optional.of("요약에 링크나 계정 이름이 있습니다.");
        }
        if (!hasEvidence(target, answer.evidence())) {
            return Optional.of("요약의 근거를 게시물 글에서 찾지 못했습니다.");
        }
        return Optional.empty();
    }

    /**
     * 인용한 구절이 그 키워드의 게시물 글 하나에 실제로 있는지. 공백은 빼고 비교하고, 두 글에 걸친 인용은 인정하지 않는다.
     * 키워드(해시태그)만 인용한 것은 근거로 보지 않는다.
     */
    private static boolean hasEvidence(Target target, String evidence) {
        String quote = compact(evidence);
        if (quote.isEmpty() || withoutHash(quote).equals(withoutHash(compact(target.keyword())))) {
            return false;
        }
        return target.posts().stream()
                .flatMap(post -> Stream.of(Objects.toString(post.title(), ""), Objects.toString(post.description(), "")))
                .anyMatch(text -> compact(text).contains(quote));
    }

    private static String compact(String text) {
        return text == null ? "" : SPACES.matcher(Normalizer.normalize(text, Normalizer.Form.NFC)).replaceAll("");
    }

    private static String withoutHash(String text) {
        return text.replace("#", "").toLowerCase(Locale.ROOT);
    }
}
