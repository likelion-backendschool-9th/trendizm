package com.trendizm.backend.lookup.adapter.in.web;

/**
 * 요청에서 받는 주소(출처·썸네일) 검증 정규식. @Pattern(regexp = WebUrlPattern.OPTIONAL)처럼 쓴다.
 * - http://, https://만 받는다. javascript: 같은 주소가 링크로 나가면 클릭할 때 스크립트가 실행된다(XSS).
 * - 공백·제어 문자·폭 없는 문자(U+200B 등)·한글 채움 문자(ㅤ)가 들어간 주소는 받지 않는다.
 *   눈에 보이지 않는 글자 하나로 같은 주소가 다른 문서가 되는 것을 막는다.
 */
public final class WebUrlPattern {

    private static final String URL = "https?://[^\\s\\p{Cc}\\p{Cf}\\u115F\\u1160\\u3164\\uFFA0\\u2800]+";

    /** 비어 있거나(선택 값) 올바른 주소. 대소문자는 무시한다 */
    public static final String OPTIONAL = "(?i)\\s*|" + URL;

    private WebUrlPattern() {
    }
}
