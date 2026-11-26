package com.trendizm.backend.lookup.adapter.in.web.dto;

import com.trendizm.backend.lookup.adapter.in.web.TextPattern;
import com.trendizm.backend.lookup.application.dto.CreateMemeAccuseCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 밈 문서 신고. 예: { "reason": "hate", "description": "특정 지역을 비하하는 단어입니다." }
 * reason은 "sexual"(선정적), "hate"(혐오·비하), "ad"(광고), "etc"(기타). 기타면 description 필수.
 */
public record CreateMemeAccuseRequest(
        @NotBlank(message = "신고 사유를 골라 주세요.")
        String reason,

        @Pattern(regexp = TextPattern.NO_CONTROL, message = "신고 내용에 쓸 수 없는 문자가 들어 있습니다.")
        @Size(max = 500, message = "신고 내용은 500자 이하여야 합니다.")
        String description
) {

    /** 보이지 않는 글자(공백, 폭 없는 문자 등)만 있는 내용은 쓰지 않은 것으로 보고 null로 넘긴다 */
    public CreateMemeAccuseCommand toCommand(Long memeId, Long userId) {
        String written = description == null || !description.matches(TextPattern.HAS_VISIBLE)
                ? null
                : description.strip();
        return new CreateMemeAccuseCommand(memeId, userId, reason, written);
    }
}
