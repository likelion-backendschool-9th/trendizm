package com.trendizm.backend.lookup.application.dto;

import com.trendizm.backend.lookup.domain.entity.MemeInterpretation;
import java.time.LocalDateTime;

/** 밈 의견. 작성자 닉네임은 서비스에서 채운다: from(interpretation).withAuthor(...) */
public record MemeInterpretationResponse(
        Long id,
        Long memeId,
        Long userId,
        String authorNickname,
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
                null,
                interpretation.getMeaning(),
                interpretation.getContent(),
                interpretation.getAgreeCount(),
                interpretation.getDisagreeCount(),
                interpretation.getCreatedAt(),
                interpretation.getUpdatedAt());
    }

    /** 탈퇴했거나 없는 회원이면 "탈퇴한 회원" */
    public MemeInterpretationResponse withAuthor(String authorNickname) {
        return new MemeInterpretationResponse(id, memeId, userId, authorNickname, meaning, content,
                agreeCount, disagreeCount, createdAt, updatedAt);
    }
}
