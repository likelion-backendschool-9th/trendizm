package com.trendizm.backend.rating.application.dto;

import com.trendizm.backend.rating.domain.entity.Rating;
import java.time.LocalDateTime;

/**
 * 후기 하나. mine은 보는 사람이 작성자인지다(로그인하지 않았으면 false).
 * 작성자 닉네임은 서비스에서 채운다: from(rating).withAuthor(...).withMine(...)
 */
public record RatingReviewResponse(
        Long id,
        Long contentId,
        SentimentInfo sentiment,
        int score,
        String title,
        String body,
        Integer progress,
        String authorNickname,
        boolean mine,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static RatingReviewResponse from(Rating rating) {
        return new RatingReviewResponse(rating.getId(), rating.getContentId(), SentimentInfo.of(rating.getSentiment()),
                rating.getScore(), rating.getTitle(), rating.getBody(), rating.getProgress(), null, false,
                rating.getCreatedAt(), rating.getUpdatedAt());
    }

    public RatingReviewResponse withAuthor(String authorNickname) {
        return new RatingReviewResponse(id, contentId, sentiment, score, title, body, progress, authorNickname, mine,
                createdAt, updatedAt);
    }

    public RatingReviewResponse withMine(boolean mine) {
        return new RatingReviewResponse(id, contentId, sentiment, score, title, body, progress, authorNickname, mine,
                createdAt, updatedAt);
    }
}
