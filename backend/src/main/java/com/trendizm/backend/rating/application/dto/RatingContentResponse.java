package com.trendizm.backend.rating.application.dto;

import com.trendizm.backend.rating.domain.entity.RatingContent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 콘텐츠 상세. 호/불호 집계는 서비스에서 채운다: from(content).withStats(...)
 * 집단 특성 TOP5는 회원 프로필 조회(MemberService.findProfiles)가 생기면 추가한다.
 */
public record RatingContentResponse(
        Long id,
        RatingCategoryInfo category,
        RatingCategoryInfo subcategory,
        RatingStatusInfo status,
        String title,
        String description,
        String imageUrl,
        Integer ageRating,
        LocalDate releasedAt,
        Map<String, Object> extra,
        SentimentStats stats,
        LocalDateTime createdAt
) {

    public static RatingContentResponse from(RatingContent content) {
        return new RatingContentResponse(
                content.getId(),
                RatingCategoryInfo.of(content.getCategory()),
                RatingCategoryInfo.of(content.getSubcategory()),
                RatingStatusInfo.of(content),
                content.getTitle(),
                content.getDescription(),
                content.getImageUrl(),
                content.getAgeRating(),
                content.getReleasedAt(),
                content.getExtra() == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(content.getExtra())),
                SentimentStats.EMPTY,
                content.getCreatedAt());
    }

    public RatingContentResponse withStats(SentimentStats stats) {
        return new RatingContentResponse(id, category, subcategory, status, title, description, imageUrl, ageRating,
                releasedAt, extra, stats, createdAt);
    }
}
