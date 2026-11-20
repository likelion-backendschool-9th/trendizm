package com.trendizm.backend.item.application.dto;

import com.trendizm.backend.item.domain.enums.ItemCategory;
import com.trendizm.backend.item.domain.enums.ItemSubcategory;

/**
 * 질문 글 목록 조건. category·subcategory는 비어 있으면 전체다.
 * 둘 다 있으면 소분류가 그 대분류에 속해야 한다(아니면 ITEM_CATEGORY_MISMATCH).
 */
public record ItemQuestionQuery(ItemCategory category, ItemSubcategory subcategory, int page, int size) {
}
