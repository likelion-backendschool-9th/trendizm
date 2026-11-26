package com.trendizm.backend.accuse.application.dto;

import com.trendizm.backend.accuse.domain.entity.Accuse;
import java.time.LocalDateTime;

/**
 * 신고 결과 (신고한 사용자에게 주는 응답). reason은 slug(예: "hate")다.
 * 대상 테이블 이름(target)·대상 id·신고한 회원 id는 내보내지 않는다(DB 구조를 드러내지 않고, 화면도 쓰지 않는다)
 */
public record AccuseResponse(
        Long id,
        String reason,
        String description,
        LocalDateTime createdAt
) {

    public static AccuseResponse from(Accuse accuse) {
        return new AccuseResponse(
                accuse.getId(),
                accuse.getReason().slug(),
                accuse.getDescription(),
                accuse.getCreatedAt());
    }
}
