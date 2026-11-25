package com.trendizm.backend.rating.application.dto;

import com.trendizm.backend.rating.domain.entity.RatingContent;
import java.time.LocalDate;
import java.util.List;

/**
 * 콘텐츠 목록의 한 줄. 호/불호 집계와 미리보기 후기는 서비스에서 묶음 조회로 채운다:
 * from(content).withStats(...).withPreviews(...)
 * likePreviews·dislikePreviews는 좋아요·싫어요 후기 중 공감 많은 순 최대 2개다(같으면 최신 후기).
 * 목록은 보는 사람을 받지 않아서 후기의 mine·likedByMe는 항상 false다.
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
        SentimentStats stats,
        List<RatingReviewResponse> likePreviews,
        List<RatingReviewResponse> dislikePreviews
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
                SentimentStats.EMPTY,
                List.of(),
                List.of());
    }

    public RatingContentSummaryResponse withStats(SentimentStats stats) {
        return new RatingContentSummaryResponse(id, category, subcategory, status, title, imageUrl, ageRating,
                releasedAt, stats, likePreviews, dislikePreviews);
    }

    public RatingContentSummaryResponse withPreviews(List<RatingReviewResponse> likePreviews,
                                                     List<RatingReviewResponse> dislikePreviews) {
        return new RatingContentSummaryResponse(id, category, subcategory, status, title, imageUrl, ageRating,
                releasedAt, stats, likePreviews, dislikePreviews);
    }
}
