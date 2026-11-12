package com.trendizm.backend.rating.application.dto;

import com.trendizm.backend.rating.domain.Rating;

/** 컬럼이 정해지면 응답 필드를 추가한다. */
public record RatingResponse(Long id) {

    public static RatingResponse from(Rating rating) {
        return new RatingResponse(rating.getId());
    }
}
