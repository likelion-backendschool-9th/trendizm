package com.trendizm.backend.ranking.application.dto;

import com.trendizm.backend.ranking.domain.entity.TrendRankingCategory;

/** 트렌드 순위 카테고리. 순위 조회(GET /api/rankings)의 category 값은 slug이고, 응답의 categoryId가 이 id다 */
public record RankingCategoryResponse(Long id, String slug, String title) {

    public static RankingCategoryResponse from(TrendRankingCategory category) {
        return new RankingCategoryResponse(category.getId(), category.getSlug(), category.getTitle());
    }
}
