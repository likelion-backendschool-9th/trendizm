package com.trendizm.backend.lookup.application.dto;

/** 이미 있는 문서(목록에서 고른 문서)에 의견을 쓴다 */
public record AddMemeInterpretationCommand(Long memeId, Long userId, String meaning, String content) {
}
