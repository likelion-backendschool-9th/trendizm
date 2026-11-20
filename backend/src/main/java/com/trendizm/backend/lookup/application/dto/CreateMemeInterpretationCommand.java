package com.trendizm.backend.lookup.application.dto;

/**
 * 의견 작성 요청을 서비스로 넘기는 값. word는 사용자가 입력한 단어 그대로이고 서비스에서 Meme.normalizeWord로 바꾼다.
 * 출처 값(sourceUrl, title, thumbnailUrl)은 이 의견으로 문서가 새로 만들어질 때만 쓴다. 없으면 null이다.
 */
public record CreateMemeInterpretationCommand(
        String word,
        Long userId,
        String meaning,
        String content,
        String sourceUrl,
        String title,
        String thumbnailUrl
) {
}
