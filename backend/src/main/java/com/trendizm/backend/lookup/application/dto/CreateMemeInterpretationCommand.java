package com.trendizm.backend.lookup.application.dto;

/**
 * 단어·출처로 의견을 쓴다. 같은 단어·출처의 문서가 있으면 거기에 붙이고, 없으면 문서를 만든다.
 * word·sourceUrl은 사용자가 입력한 그대로이고 서비스에서 정규화한다. 제목·썸네일은 새 문서를 만들 때만 쓰고 없으면 null이다.
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
