package com.trendizm.backend.lookup.application.dto;

import com.trendizm.backend.lookup.domain.entity.MemeInterpretation;
import java.time.LocalDateTime;

/** 밈 의견. 작성자는 회원 id만 준다 */
public record MemeInterpretationResponse(
        Long id,
        Long memeId,
        Long userId,
        String meaning,
        String content,
        int agreeCount,
        int disagreeCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static MemeInterpretationResponse from(MemeInterpretation interpretation) {
        return new MemeInterpretationResponse(
                interpretation.getId(),
                interpretation.getMemeId(),
                interpretation.getUserId(),
                interpretation.getMeaning(),
                interpretation.getContent(),
                interpretation.getAgreeCount(),
                interpretation.getDisagreeCount(),
                interpretation.getCreatedAt(),
                interpretation.getUpdatedAt());
    }
}
