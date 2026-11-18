package com.trendizm.backend.item.application.dto;

import com.trendizm.backend.item.domain.enums.ItemCategory;
import com.trendizm.backend.item.domain.enums.ItemSubcategory;

/**
 * 응답에 넣는 대분류·소분류 정보.
 * code는 API 요청에 쓰는 값(예: FASHION), slug는 화면 주소에 쓰는 값(예: fashion), label은 화면 이름(예: 패션)이다.
 */
public record ItemCategoryInfo(String code, String slug, String label) {

    public static ItemCategoryInfo of(ItemCategory category) {
        return new ItemCategoryInfo(category.name(), category.getSlug(), category.getLabel());
    }

    public static ItemCategoryInfo of(ItemSubcategory subcategory) {
        return new ItemCategoryInfo(subcategory.name(), subcategory.getSlug(), subcategory.getLabel());
    }
}
