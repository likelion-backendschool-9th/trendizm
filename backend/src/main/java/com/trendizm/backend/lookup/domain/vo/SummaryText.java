package com.trendizm.backend.lookup.domain.vo;

/**
 * AI 요약 글 다듬기. 길이 제한에 걸려 중간에 잘린 글이 문장 중간에서 끝나지 않게 한다.
 * - AI 응답이 출력 토큰 상한(MAX_TOKENS)에 걸려 끊긴 경우: GeminiSummaryGenerator
 * - 저장 길이(500자)보다 길어 서버가 자르는 경우: MemeSummaryServiceImpl
 */
public final class SummaryText {

    /** 완성 문장을 하나도 찾지 못해 글자 단위로 잘랐을 때 끝에 붙인다 */
    public static final String ELLIPSIS = "…";

    private SummaryText() {
    }

    /**
     * 잘린 글을 마지막 완성 문장까지만 남긴다. 완성 문장이 없으면 끝에 말줄임표를 붙인다.
     * 문장 끝은 . ! ? 。 뒤가 글 끝이거나 공백인 곳이다("1.5"의 점은 문장 끝이 아니다).
     */
    public static String endAtSentence(String cutText) {
        String text = cutText.strip();
        for (int i = text.length() - 1; i >= 0; i--) {
            if (isSentenceEnd(text.charAt(i)) && (i == text.length() - 1 || Character.isWhitespace(text.charAt(i + 1)))) {
                return text.substring(0, i + 1);
            }
        }
        return text + ELLIPSIS;
    }

    /** maxLength 이하면 그대로 둔다. 넘으면 maxLength 안에서 자른 뒤 endAtSentence로 끝을 다듬는다(결과도 maxLength 이하) */
    public static String fit(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        int end = maxLength - ELLIPSIS.length();
        if (Character.isHighSurrogate(text.charAt(end - 1))) {
            end--; // 이모지 같은 2칸 문자를 반으로 자르지 않는다
        }
        return endAtSentence(text.substring(0, end));
    }

    private static boolean isSentenceEnd(char c) {
        return c == '.' || c == '!' || c == '?' || c == '。';
    }
}
