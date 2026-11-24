package com.trendizm.backend.lookup.application.port.out;

import java.util.Collection;
import java.util.List;

/** 구현체는 adapter/out/persistence/JpaMemeCommentLikeRepository (Spring Data가 자동 구현) */
public interface MemeCommentLikeRepository {

    /**
     * 좋아요를 추가한다. 이미 있으면 아무것도 하지 않는다(동시에 눌러도 한 줄만 생긴다).
     * @return 추가한 행 수 (0 또는 1)
     */
    int insertIfAbsent(Long commentId, Long userId);

    /** @return 지운 행 수 */
    int deleteByCommentIdAndUserId(Long commentId, Long userId);

    /** 댓글을 지우거나 삭제 표시할 때 그 댓글의 좋아요를 모두 지운다 */
    int deleteByCommentId(Long commentId);

    /** 댓글 id별 좋아요 수. 좋아요가 없는 댓글은 결과에 없다 */
    List<CountByKey> countGroupByCommentId(Collection<Long> commentIds);

    /** 이 회원이 좋아요한 댓글 id (주어진 댓글 중에서) */
    List<Long> findLikedCommentIds(Long userId, Collection<Long> commentIds);
}
