package com.trendizm.backend.item.application.dto;

import com.trendizm.backend.item.domain.enums.ItemSubcategory;

/** 질문 글 작성. 대분류는 소분류에서 정해진다 */
public record CreateItemQuestionCommand(Long userId, ItemSubcategory subcategory, String title, String content) {
}
