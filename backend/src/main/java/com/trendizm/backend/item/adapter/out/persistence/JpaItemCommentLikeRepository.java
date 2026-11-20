package com.trendizm.backend.item.adapter.out.persistence;

import com.trendizm.backend.item.application.port.out.CountByKey;
import com.trendizm.backend.item.application.port.out.ItemCommentLikeRepository;
import com.trendizm.backend.item.domain.entity.ItemCommentLike;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** ItemCommentLikeRepository(out 포트)의 메서드를 Spring Data가 구현한다 */
public interface JpaItemCommentLikeRepository extends JpaRepository<ItemCommentLike, Long>, ItemCommentLikeRepository {

    /**
     * (comment_id, user_id) 유니크 제약에 걸리면 넣지 않는다. 충돌 대상을 적지 않은 형태는 H2도 지원한다.
     * 네이티브 INSERT라 JPA Auditing이 동작하지 않아서 created_at을 직접 채운다.
     */
    @Override
    @Modifying
    @Query(value = """
            INSERT INTO item_comment_likes (comment_id, user_id, created_at)
            VALUES (:commentId, :userId, CURRENT_TIMESTAMP)
            ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("commentId") Long commentId, @Param("userId") Long userId);

    @Override
    @Modifying
    @Query("delete from ItemCommentLike l where l.commentId = :commentId and l.userId = :userId")
    int deleteByCommentIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);

    @Override
    @Query("""
            select new com.trendizm.backend.item.application.port.out.CountByKey(l.commentId, count(l))
            from ItemCommentLike l
            where l.commentId in :commentIds
            group by l.commentId
            """)
    List<CountByKey> countGroupByCommentId(@Param("commentIds") Collection<Long> commentIds);

    @Override
    @Query("select l.commentId from ItemCommentLike l where l.userId = :userId and l.commentId in :commentIds")
    List<Long> findLikedCommentIds(@Param("userId") Long userId, @Param("commentIds") Collection<Long> commentIds);
}
