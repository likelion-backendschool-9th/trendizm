package com.trendizm.backend.lookup.application.dto;

/** 밈 문서 신고. reason은 "sexual", "hate", "ad", "false_info", "etc" 중 하나, description은 없으면 null */
public record CreateMemeAccuseCommand(
        Long memeId,
        Long userId,
        String reason,
        String description
) {
}
