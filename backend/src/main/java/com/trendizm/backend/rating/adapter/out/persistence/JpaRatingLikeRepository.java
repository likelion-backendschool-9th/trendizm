package com.trendizm.backend.rating.adapter.out.persistence;

import com.trendizm.backend.rating.application.port.out.RatingLikeRepository;
import com.trendizm.backend.rating.application.port.out.ReviewLikeCount;
import com.trendizm.backend.rating.domain.entity.RatingLike;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** RatingLikeRepository(out 포트)의 메서드를 Spring Data가 구현한다 */
public interface JpaRatingLikeRepository extends JpaRepository<RatingLike, Long>, RatingLikeRepository {

    /**
     * (rating_id, user_id) 유니크 제약에 걸리면 넣지 않는다. 충돌 대상을 적지 않은 형태는 H2도 지원한다.
     * 네이티브 INSERT라 JPA Auditing이 동작하지 않아서 created_at을 직접 채운다.
     */
    @Override
    @Modifying
    @Query(value = """
            INSERT INTO rating_likes (rating_id, user_id, created_at)
            VALUES (:ratingId, :userId, CURRENT_TIMESTAMP)
            ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("ratingId") Long ratingId, @Param("userId") Long userId);

    @Override
    @Modifying
    @Query("delete from RatingLike l where l.ratingId = :ratingId and l.userId = :userId")
    int deleteByRatingIdAndUserId(@Param("ratingId") Long ratingId, @Param("userId") Long userId);

    @Override
    @Query("""
            select new com.trendizm.backend.rating.application.port.out.ReviewLikeCount(l.ratingId, count(l))
            from RatingLike l
            where l.ratingId in :ratingIds
            group by l.ratingId
            """)
    List<ReviewLikeCount> countGroupByRatingId(@Param("ratingIds") Collection<Long> ratingIds);

    @Override
    @Query("select l.ratingId from RatingLike l where l.userId = :userId and l.ratingId in :ratingIds")
    List<Long> findLikedRatingIds(@Param("userId") Long userId, @Param("ratingIds") Collection<Long> ratingIds);
}
