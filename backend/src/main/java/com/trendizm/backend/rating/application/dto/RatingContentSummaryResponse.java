package com.trendizm.backend.rating.application.dto;

import com.trendizm.backend.rating.domain.entity.RatingContent;
import java.time.LocalDate;

/**
 * 콘텐츠 목록의 한 줄. 호/불호 집계는 서비스에서 묶음 조회로 채운다: from(content).withStats(...)
 */
public record RatingContentSummaryResponse(
        Long id,
        RatingCategoryInfo category,
        RatingCategoryInfo subcategory,
        RatingStatusInfo status,
        String title,
        String imageUrl,
        Integer ageRating,
        LocalDate releasedAt,
        SentimentStats stats
) {

    public static RatingContentSummaryResponse from(RatingContent content) {
        return new RatingContentSummaryResponse(
                content.getId(),
                RatingCategoryInfo.of(content.getCategory()),
                RatingCategoryInfo.of(content.getSubcategory()),
                RatingStatusInfo.of(content),
                content.getTitle(),
                content.getImageUrl(),
                content.getAgeRating(),
                content.getReleasedAt(),
                SentimentStats.EMPTY);
    }

    public RatingContentSummaryResponse withStats(SentimentStats stats) {
        return new RatingContentSummaryResponse(id, category, subcategory, status, title, imageUrl, ageRating,
                releasedAt, stats);
    }
}
