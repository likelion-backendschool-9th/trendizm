package com.trendizm.backend.lookup.adapter.in.web.dto;

import com.trendizm.backend.lookup.adapter.in.web.TextPattern;
import com.trendizm.backend.lookup.application.dto.UpdateMemeCommentCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** 댓글 수정. 예: { "content": "..." }. 내용 검증 규칙은 작성(CreateMemeCommentRequest)과 같다 */
public record UpdateMemeCommentRequest(
        @NotBlank(message = "내용을 입력해 주세요.")
        @Pattern(regexp = TextPattern.HAS_VISIBLE, message = "내용을 입력해 주세요.")
        @Pattern(regexp = TextPattern.NO_CONTROL, message = "내용에 쓸 수 없는 문자가 들어 있습니다.")
        @Size(max = 500, message = "댓글은 500자 이하여야 합니다.")
        String content
) {

    public UpdateMemeCommentCommand toCommand(Long commentId, Long userId) {
        return new UpdateMemeCommentCommand(commentId, userId, content.strip());
    }
}
