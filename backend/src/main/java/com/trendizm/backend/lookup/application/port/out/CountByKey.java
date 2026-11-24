package com.trendizm.backend.lookup.application.port.out;

/** id별 개수 묶음 조회 결과. 예: 댓글 id별 좋아요 수 */
public record CountByKey(Long key, long count) {
}
