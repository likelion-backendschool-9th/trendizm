package com.trendizm.backend.lookup.adapter.in.web.dto;

import com.trendizm.backend.lookup.adapter.in.web.WebUrlPattern;
import com.trendizm.backend.lookup.application.dto.CreateMemeInterpretationCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 단어·출처로 의견 작성. 같은 단어·출처의 문서가 있으면 거기에 붙고, 없으면 문서도 함께 만든다.
 * 단어는 경로가 아니라 본문으로 받는다('/'·'%'가 들어간 단어 때문). 출처 주소는 필수다.
 * 제목·썸네일은 선택이고, 이 의견으로 문서가 새로 만들어질 때만 저장된다.
 * 주소 검증 규칙은 WebUrlPattern을 따른다(http·https만, 보이지 않는 문자 금지).
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

        @NotBlank(message = "출처 주소를 입력해 주세요.")
        @Size(max = 500, message = "출처 주소는 500자 이하여야 합니다.")
        @Pattern(regexp = WebUrlPattern.OPTIONAL, message = "출처 주소는 http:// 또는 https://로 시작하는 올바른 주소여야 합니다.")
        String sourceUrl,

        @Size(max = 255, message = "출처 제목은 255자 이하여야 합니다.")
        String title,

        @Size(max = 500, message = "썸네일 주소는 500자 이하여야 합니다.")
        @Pattern(regexp = WebUrlPattern.OPTIONAL, message = "썸네일 주소는 http:// 또는 https://로 시작하는 올바른 주소여야 합니다.")
        String thumbnailUrl
) {

    public CreateMemeInterpretationCommand toCommand(Long userId) {
        return new CreateMemeInterpretationCommand(
                word, userId, meaning.strip(), content.strip(), sourceUrl.strip(), blankToNull(title), blankToNull(thumbnailUrl));
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.strip();
    }
}
