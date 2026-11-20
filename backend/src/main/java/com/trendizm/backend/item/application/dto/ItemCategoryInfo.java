package com.trendizm.backend.item.application.dto;

import com.trendizm.backend.item.domain.enums.ItemCategory;
import com.trendizm.backend.item.domain.enums.ItemSubcategory;

/**
 * 응답에 넣는 대분류·소분류 정보.
 * slug는 API 요청과 화면 주소에 쓰는 값(예: eyewear-accessories), label은 화면 이름(예: 안경·액세서리)이다.
 */
public record ItemCategoryInfo(String slug, String label) {

    public static ItemCategoryInfo of(ItemCategory category) {
        return new ItemCategoryInfo(category.getSlug(), category.getLabel());
    }

    public static ItemCategoryInfo of(ItemSubcategory subcategory) {
        return new ItemCategoryInfo(subcategory.getSlug(), subcategory.getLabel());
    }
}
