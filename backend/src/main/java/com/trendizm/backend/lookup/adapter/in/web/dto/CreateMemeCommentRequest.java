package com.trendizm.backend.lookup.adapter.in.web.dto;

import com.trendizm.backend.lookup.adapter.in.web.TextPattern;
import com.trendizm.backend.lookup.application.dto.CreateMemeCommentCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 댓글 작성. parentId를 보내면 그 댓글에 다는 대댓글이다. 예: { "parentId": 3, "content": "..." }
 * 내용 검증 규칙은 TextPattern을 따른다(보이는 글자 필수, 제어 문자 금지).
 */
public record CreateMemeCommentRequest(
        Long parentId,
        @NotBlank(message = "내용을 입력해 주세요.")
        @Pattern(regexp = TextPattern.HAS_VISIBLE, message = "내용을 입력해 주세요.")
        @Pattern(regexp = TextPattern.NO_CONTROL, message = "내용에 쓸 수 없는 문자가 들어 있습니다.")
        @Size(max = 500, message = "댓글은 500자 이하여야 합니다.")
        String content
) {

    public CreateMemeCommentCommand toCommand(Long interpretationId, Long userId) {
        return new CreateMemeCommentCommand(interpretationId, parentId, userId, content.strip());
    }
}
