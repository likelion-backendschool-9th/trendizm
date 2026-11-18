package com.trendizm.backend.item.adapter.in.web.dto;

import com.trendizm.backend.item.application.dto.CreateItemQuestionCommand;
import com.trendizm.backend.item.domain.enums.ItemSubcategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 질문 글 작성. 예: { "subcategory": "CLOTHING", "title": "양말 추천해주세요", "content": "..." } */
public record CreateItemQuestionRequest(
        @NotNull(message = "소분류를 선택해 주세요.")
        ItemSubcategory subcategory,

        @NotBlank(message = "제목을 입력해 주세요.")
        @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
        String title,

        @NotBlank(message = "내용을 입력해 주세요.")
        @Size(max = 5000, message = "내용은 5000자 이하여야 합니다.")
        String content
) {

    public CreateItemQuestionCommand toCommand(Long userId) {
        return new CreateItemQuestionCommand(userId, subcategory, title.strip(), content.strip());
    }
}
