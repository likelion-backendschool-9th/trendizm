package com.trendizm.backend.lookup.adapter.out.persistence;

import com.trendizm.backend.lookup.application.port.out.CountByKey;
import com.trendizm.backend.lookup.application.port.out.MemeCommentLikeRepository;
import com.trendizm.backend.lookup.domain.entity.MemeCommentLike;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** MemeCommentLikeRepository(out 포트)의 메서드를 Spring Data가 구현한다 */
public interface JpaMemeCommentLikeRepository extends JpaRepository<MemeCommentLike, Long>, MemeCommentLikeRepository {

    /**
     * (comment_id, user_id) 유니크 제약에 걸리면 넣지 않는다. 충돌 대상을 적지 않은 형태는 H2도 지원한다.
     * 네이티브 INSERT라 JPA Auditing이 동작하지 않아서 created_at을 직접 채운다.
     */
    @Override
    @Modifying
    @Query(value = """
            INSERT INTO meme_comment_likes (comment_id, user_id, created_at)
            VALUES (:commentId, :userId, CURRENT_TIMESTAMP)
            ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("commentId") Long commentId, @Param("userId") Long userId);

    @Override
    @Modifying
    @Query("delete from MemeCommentLike l where l.commentId = :commentId and l.userId = :userId")
    int deleteByCommentIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);

    @Override
    @Modifying
    @Query("delete from MemeCommentLike l where l.commentId = :commentId")
    int deleteByCommentId(@Param("commentId") Long commentId);

    @Override
    @Query("""
            select new com.trendizm.backend.lookup.application.port.out.CountByKey(l.commentId, count(l))
            from MemeCommentLike l
            where l.commentId in :commentIds
            group by l.commentId
            """)
    List<CountByKey> countGroupByCommentId(@Param("commentIds") Collection<Long> commentIds);

    @Override
    @Query("select l.commentId from MemeCommentLike l where l.userId = :userId and l.commentId in :commentIds")
    List<Long> findLikedCommentIds(@Param("userId") Long userId, @Param("commentIds") Collection<Long> commentIds);
}
