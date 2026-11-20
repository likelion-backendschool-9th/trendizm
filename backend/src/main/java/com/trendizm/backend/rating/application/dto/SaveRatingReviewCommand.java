package com.trendizm.backend.rating.application.dto;

import com.trendizm.backend.rating.domain.enums.Sentiment;

/** 후기 작성·수정. 이 회원이 이 콘텐츠에 쓴 후기가 있으면 수정하고, 없으면 새로 만든다 */
public record SaveRatingReviewCommand(Long contentId, Long userId, Sentiment sentiment, int score, String title,
                                      String body, Integer progress) {
}
