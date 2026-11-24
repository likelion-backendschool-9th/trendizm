package com.trendizm.backend.lookup.application.port;

import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.lookup.application.dto.CreateMemeCommentCommand;
import com.trendizm.backend.lookup.application.dto.MemeCommentResponse;
import com.trendizm.backend.lookup.application.dto.UpdateMemeCommentCommand;

/** 밈 의견의 댓글·대댓글과 댓글 좋아요 */
public interface MemeCommentService {

    /**
     * 댓글 목록 (최신순). 댓글마다 대댓글을 오래된 순으로 함께 담는다. page·size는 댓글(대댓글 제외) 기준.
     * viewerId는 로그인한 사용자 id(likedByMe 표시용)이고 비로그인이면 null.
     * 의견이 없거나 문서가 숨김이면 MEME_INTERPRETATION_NOT_FOUND
     */
    PageResponse<MemeCommentResponse> getComments(Long interpretationId, Long viewerId, int page, int size);

    /**
     * 댓글을 쓴다. 대댓글은 같은 의견의 삭제되지 않은 댓글에만 달 수 있다(대댓글에 답글 불가). 아니면 MEME_INVALID_PARENT_COMMENT.
     * 의견이 없거나 문서가 숨김이면 MEME_INTERPRETATION_NOT_FOUND
     */
    MemeCommentResponse createComment(CreateMemeCommentCommand command);

    /** 내 댓글을 고친다. 없거나 삭제된 댓글이면 MEME_COMMENT_NOT_FOUND, 남의 댓글이면 MEME_COMMENT_FORBIDDEN */
    MemeCommentResponse updateComment(UpdateMemeCommentCommand command);

    /**
     * 내 댓글을 지운다. 대댓글이 남아 있는 댓글은 "삭제된 댓글"로 흔적만 남기고, 아니면 행을 지운다.
     * 대댓글을 지워서 흔적만 남은 부모 댓글에 대댓글이 하나도 남지 않으면 부모도 지운다.
     * 없거나 삭제된 댓글이면 MEME_COMMENT_NOT_FOUND, 남의 댓글이면 MEME_COMMENT_FORBIDDEN
     */
    void deleteComment(Long commentId, Long userId);

    /** 이미 좋아요했으면 아무것도 하지 않는다. 없거나 삭제된 댓글이면 MEME_COMMENT_NOT_FOUND */
    void likeComment(Long commentId, Long userId);

    /** 좋아요하지 않았으면 아무것도 하지 않는다 */
    void unlikeComment(Long commentId, Long userId);
}
