package com.trendizm.backend.item.application.port.out;

/** id별 개수 묶음 조회 결과. 예: 질문 id별 댓글 수, 댓글 id별 공감 수 */
public record CountByKey(Long key, long count) {
}
