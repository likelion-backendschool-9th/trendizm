package com.trendizm.backend.rating.application.dto;

/**
 * 후기 저장 결과. created는 새로 만들었는지(false면 기존 후기를 수정), rewardPoint는 이번에 적립한 포인트다.
 * 보상은 후기마다 처음 작성할 때 한 번만 준다(수정하면 0).
 */
public record RatingReviewSaveResult(RatingReviewResponse review, boolean created, int rewardPoint) {
}
