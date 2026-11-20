package com.trendizm.backend.rating.application.dto;

import com.trendizm.backend.rating.domain.entity.RatingContent;

/** 응답에 넣는 진행 상태. slug는 API 요청값(예: ongoing), label은 대분류에 맞는 화면 이름(예: 방영중) */
public record RatingStatusInfo(String slug, String label) {

    public static RatingStatusInfo of(RatingContent content) {
        return new RatingStatusInfo(content.getStatus().getSlug(), content.getStatusLabel());
    }
}
