package com.trendizm.backend.rating.application.dto;

import com.trendizm.backend.rating.domain.enums.RatingContentCategory;
import com.trendizm.backend.rating.domain.enums.RatingContentSubcategory;

/**
 * 응답에 넣는 대분류·소분류 정보.
 * slug는 API 요청과 화면 주소에 쓰는 값(예: concert-festival), label은 화면 이름(예: 공연·페스티벌)이다.
 */
public record RatingCategoryInfo(String slug, String label) {

    public static RatingCategoryInfo of(RatingContentCategory category) {
        return new RatingCategoryInfo(category.getSlug(), category.getLabel());
    }

    public static RatingCategoryInfo of(RatingContentSubcategory subcategory) {
        return new RatingCategoryInfo(subcategory.getSlug(), subcategory.getLabel());
    }
}
