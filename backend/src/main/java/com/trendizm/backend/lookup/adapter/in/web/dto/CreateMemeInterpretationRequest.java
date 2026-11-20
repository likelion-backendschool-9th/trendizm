package com.trendizm.backend.lookup.adapter.in.web.dto;

import com.trendizm.backend.lookup.application.dto.CreateMemeInterpretationCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 의견 작성. 단어는 경로가 아니라 본문으로 받는다('/'·'%'가 들어간 단어 때문).
 * 출처 값은 선택이고, 이 의견으로 문서가 새로 만들어질 때만 문서의 대표 출처로 저장된다.
 * 예: { "word": "알잘딱깔센", "meaning": "알아서 잘 딱 깔끔하고 센스 있게", "content": "...", "sourceUrl": "https://..." }
 */
public record CreateMemeInterpretationRequest(
        @NotBlank(message = "단어를 입력해 주세요.")
        @Size(max = 100, message = "단어는 100자 이하여야 합니다.")
        String word,

        @NotBlank(message = "뜻을 입력해 주세요.")
        @Size(max = 100, message = "뜻은 100자 이하여야 합니다.")
        String meaning,

        @NotBlank(message = "내용을 입력해 주세요.")
        @Size(max = 5000, message = "내용은 5000자 이하여야 합니다.")
        String content,

        @Size(max = 500, message = "출처 주소는 500자 이하여야 합니다.")
        String sourceUrl,

        @Size(max = 255, message = "출처 제목은 255자 이하여야 합니다.")
        String title,

        @Size(max = 500, message = "썸네일 주소는 500자 이하여야 합니다.")
        String thumbnailUrl
) {

    public CreateMemeInterpretationCommand toCommand(Long userId) {
        return new CreateMemeInterpretationCommand(
                word, userId, meaning.strip(), content.strip(), blankToNull(sourceUrl), blankToNull(title), blankToNull(thumbnailUrl));
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.strip();
    }
}
