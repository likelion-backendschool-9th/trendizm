package com.trendizm.backend.lookup.adapter.in.web.dto;

import com.trendizm.backend.lookup.application.dto.AddMemeInterpretationCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 이미 있는 문서에 의견 작성. 예: { "meaning": "알아서 잘 딱 깔끔하고 센스 있게", "content": "..." } */
public record AddMemeInterpretationRequest(
        @NotBlank(message = "뜻을 입력해 주세요.")
        @Size(max = 100, message = "뜻은 100자 이하여야 합니다.")
        String meaning,

        @NotBlank(message = "내용을 입력해 주세요.")
        @Size(max = 5000, message = "내용은 5000자 이하여야 합니다.")
        String content
) {

    public AddMemeInterpretationCommand toCommand(Long memeId, Long userId) {
        return new AddMemeInterpretationCommand(memeId, userId, meaning.strip(), content.strip());
    }
}
