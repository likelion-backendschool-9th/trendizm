package com.trendizm.backend.rating.application.port.out;

/** 후기 id별 공감 수 (집계 조회 결과) */
public record ReviewLikeCount(Long ratingId, long count) {
}
