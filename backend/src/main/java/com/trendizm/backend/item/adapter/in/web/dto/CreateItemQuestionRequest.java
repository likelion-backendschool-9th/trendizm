package com.trendizm.backend.item.adapter.in.web.dto;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.item.application.dto.CreateItemQuestionCommand;
import com.trendizm.backend.item.application.exception.ItemErrorCode;
import com.trendizm.backend.item.domain.enums.ItemSubcategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 질문 글 작성. subcategory는 소분류 slug다.
 * 예: { "subcategory": "clothing", "title": "양말 추천해주세요", "content": "..." }
 */
public record CreateItemQuestionRequest(
        @NotBlank(message = "소분류를 선택해 주세요.")
        String subcategory,

        @NotBlank(message = "제목을 입력해 주세요.")
        @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
        String title,

        @NotBlank(message = "내용을 입력해 주세요.")
        @Size(max = 5000, message = "내용은 5000자 이하여야 합니다.")
        String content
) {

    /** 없는 소분류 slug면 ITEM_INVALID_CATEGORY */
    public CreateItemQuestionCommand toCommand(Long userId) {
        ItemSubcategory parsed = ItemSubcategory.fromSlug(subcategory.strip())
                .orElseThrow(() -> new BusinessException(ItemErrorCode.ITEM_INVALID_CATEGORY));
        return new CreateItemQuestionCommand(userId, parsed, title.strip(), content.strip());
    }
}
