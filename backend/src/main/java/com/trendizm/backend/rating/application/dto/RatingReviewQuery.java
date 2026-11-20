package com.trendizm.backend.rating.application.dto;

import com.trendizm.backend.rating.application.port.ReviewSort;
import com.trendizm.backend.rating.domain.enums.Sentiment;

/** 후기 목록 조건. sentiment가 비어 있으면 좋아요·싫어요 모두. viewerId는 로그인하지 않았으면 null */
public record RatingReviewQuery(Long contentId, Sentiment sentiment, ReviewSort sort, Long viewerId,
                                int page, int size) {
}
