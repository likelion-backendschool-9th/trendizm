package com.trendizm.backend.lookup.application.dto;

import com.trendizm.backend.lookup.domain.entity.MemeComment;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 댓글 하나. 댓글이면 replies에 대댓글이 오래된 순으로 들어 있고, 대댓글이면 replies는 비어 있다.
 * deleted가 true면 대댓글이 남아 있어 흔적만 남긴 삭제 댓글이라 userId·authorNickname·content는 null이고 좋아요는 0이다.
 * likedByMe는 보는 사람이 좋아요했는지다. 로그인하지 않았으면 false.
 * 작성자 닉네임과 좋아요 정보는 서비스에서 채운다: from(comment).withAuthor(...).withLikes(...).withReplies(...)
 */
public record MemeCommentResponse(
        Long id,
        Long parentId,
        Long userId,
        String authorNickname,
        String content,
        long likeCount,
        boolean likedByMe,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<MemeCommentResponse> replies
) {

    public static MemeCommentResponse from(MemeComment comment) {
        if (comment.isDeleted()) {
            return new MemeCommentResponse(comment.getId(), comment.getParentId(), null, null, null,
                    0, false, true, comment.getCreatedAt(), comment.getUpdatedAt(), List.of());
        }
        return new MemeCommentResponse(comment.getId(), comment.getParentId(), comment.getUserId(), null,
                comment.getContent(), 0, false, false, comment.getCreatedAt(), comment.getUpdatedAt(), List.of());
    }

    /** 삭제된 댓글이면 바꾸지 않는다 */
    public MemeCommentResponse withAuthor(String authorNickname) {
        if (deleted) {
            return this;
        }
        return new MemeCommentResponse(id, parentId, userId, authorNickname, content, likeCount, likedByMe, deleted,
                createdAt, updatedAt, replies);
    }

    /** 삭제된 댓글이면 바꾸지 않는다 */
    public MemeCommentResponse withLikes(long likeCount, boolean likedByMe) {
        if (deleted) {
            return this;
        }
        return new MemeCommentResponse(id, parentId, userId, authorNickname, content, likeCount, likedByMe, deleted,
                createdAt, updatedAt, replies);
    }

    public MemeCommentResponse withReplies(List<MemeCommentResponse> replies) {
        return new MemeCommentResponse(id, parentId, userId, authorNickname, content, likeCount, likedByMe, deleted,
                createdAt, updatedAt, List.copyOf(replies));
    }
}
