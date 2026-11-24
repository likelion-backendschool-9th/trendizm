package com.trendizm.backend.lookup.application.dto;

/** 댓글 수정. 작성자 본인만 할 수 있다 */
public record UpdateMemeCommentCommand(Long commentId, Long userId, String content) {
}
