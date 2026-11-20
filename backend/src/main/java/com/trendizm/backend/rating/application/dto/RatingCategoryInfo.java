package com.trendizm.backend.rating.application.dto;

import com.trendizm.backend.rating.domain.enums.RatingContentCategory;
import com.trendizm.backend.rating.domain.enums.RatingContentSubcategory;

/**
 * 응답에 넣는 대분류·소분류 정보.
 * code는 API 요청에 쓰는 값(예: MEDIA), slug는 화면 주소에 쓰는 값(예: media), label은 화면 이름(예: 미디어)이다.
 */
public record RatingCategoryInfo(String code, String slug, String label) {

    public static RatingCategoryInfo of(RatingContentCategory category) {
        return new RatingCategoryInfo(category.name(), category.getSlug(), category.getLabel());
    }

    public static RatingCategoryInfo of(RatingContentSubcategory subcategory) {
        return new RatingCategoryInfo(subcategory.name(), subcategory.getSlug(), subcategory.getLabel());
    }
}
