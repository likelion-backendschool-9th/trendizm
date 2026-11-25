package com.trendizm.backend.lookup.application.dto;

/** 댓글 작성. parentId가 null이면 댓글, 값이 있으면 그 댓글에 다는 대댓글 */
public record CreateMemeCommentCommand(Long interpretationId, Long parentId, Long userId, String content) {
}
