package com.trendizm.backend.item.application.dto;

import com.trendizm.backend.item.domain.entity.ItemComment;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 댓글 하나. 댓글이면 replies에 대댓글이 오래된 순으로 들어 있고, 대댓글이면 replies는 비어 있다.
 * likedByMe는 보는 사람이 공감했는지다. 로그인하지 않았으면 false.
 * 작성자 닉네임과 공감 정보는 서비스에서 채운다: from(comment).withAuthor(...).withLikes(...).withReplies(...)
 */
public record ItemCommentResponse(
        Long id,
        Long parentId,
        String content,
        String authorNickname,
        long likeCount,
        boolean likedByMe,
        LocalDateTime createdAt,
        List<ItemCommentResponse> replies
) {

    public static ItemCommentResponse from(ItemComment comment) {
        return new ItemCommentResponse(comment.getId(), comment.getParentId(), comment.getContent(),
                null, 0, false, comment.getCreatedAt(), List.of());
    }

    public ItemCommentResponse withAuthor(String authorNickname) {
        return new ItemCommentResponse(id, parentId, content, authorNickname, likeCount, likedByMe, createdAt, replies);
    }

    public ItemCommentResponse withLikes(long likeCount, boolean likedByMe) {
        return new ItemCommentResponse(id, parentId, content, authorNickname, likeCount, likedByMe, createdAt, replies);
    }

    public ItemCommentResponse withReplies(List<ItemCommentResponse> replies) {
        return new ItemCommentResponse(id, parentId, content, authorNickname, likeCount, likedByMe, createdAt,
                List.copyOf(replies));
    }
}
