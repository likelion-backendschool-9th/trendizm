package com.trendizm.backend.accuse.application.dto;

import com.trendizm.backend.accuse.domain.entity.Accuse;
import java.time.LocalDateTime;

/** 신고 결과. target·reason은 slug(예: "meme", "hate")다. 신고한 회원 id는 내보내지 않는다 */
public record AccuseResponse(
        Long id,
        String target,
        Long targetId,
        String reason,
        String description,
        LocalDateTime createdAt
) {

    public static AccuseResponse from(Accuse accuse) {
        return new AccuseResponse(
                accuse.getId(),
                accuse.getTarget().slug(),
                accuse.getTargetId(),
                accuse.getReason().slug(),
                accuse.getDescription(),
                accuse.getCreatedAt());
    }
}
