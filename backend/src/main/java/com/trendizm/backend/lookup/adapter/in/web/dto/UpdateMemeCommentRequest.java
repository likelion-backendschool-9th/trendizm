package com.trendizm.backend.lookup.adapter.in.web.dto;

import com.trendizm.backend.lookup.application.dto.UpdateMemeCommentCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 댓글 수정. 예: { "content": "..." } */
public record UpdateMemeCommentRequest(
        @NotBlank(message = "내용을 입력해 주세요.")
        @Size(max = 500, message = "댓글은 500자 이하여야 합니다.")
        String content
) {

    public UpdateMemeCommentCommand toCommand(Long commentId, Long userId) {
        return new UpdateMemeCommentCommand(commentId, userId, content.strip());
    }
}
