package com.trendizm.backend.rating.application.port.out;

import com.trendizm.backend.rating.domain.enums.Sentiment;

/** 콘텐츠·호불호별 후기 수와 평균 별점 (집계 조회 결과) */
public record SentimentCount(Long contentId, Sentiment sentiment, long count, Double averageScore) {
}
