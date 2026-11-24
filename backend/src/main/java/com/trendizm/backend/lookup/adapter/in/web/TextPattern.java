package com.trendizm.backend.lookup.adapter.in.web;

/**
 * 요청에서 받는 글(단어·뜻·내용·댓글·검색어) 검증 정규식. @Pattern(regexp = TextPattern.NO_CONTROL)처럼 쓴다.
 * null은 통과하므로 필수 값에는 @NotBlank를 함께 붙인다.
 */
public final class TextPattern {

    /**
     * 제어 문자가 없는 글. 줄바꿈(\n, \r)과 탭만 허용한다.
     * PostgreSQL은 NULL 문자(\u0000)를 text·varchar에 저장하지 못해서, 그대로 넘기면 저장·조회할 때 500이 난다(H2는 받아 줘서 테스트로는 안 보인다).
     */
    public static final String NO_CONTROL = "[^\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F-\\x9F]*";

    /**
     * 눈에 보이는 글자가 하나 이상 있는 글. @NotBlank는 일반 공백만 거르므로,
     * 공백 문자(NBSP·전각 공백 등)·폭 없는 문자(U+200B 등)·한글 채움 문자(ㅤ)·점자 빈칸만 있는 글도 빈 글로 본다.
     */
    public static final String HAS_VISIBLE =
            "(?s).*[^\\s\\p{Z}\\p{Cc}\\p{Cf}\\u115F\\u1160\\u3164\\uFFA0\\u2800].*";

    private TextPattern() {
    }
}
