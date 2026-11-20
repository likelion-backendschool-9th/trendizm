package com.trendizm.backend.rating.adapter.out.persistence;

import com.trendizm.backend.rating.application.port.out.RatingRepository;
import com.trendizm.backend.rating.application.port.out.SentimentCount;
import com.trendizm.backend.rating.domain.entity.Rating;
import com.trendizm.backend.rating.domain.enums.Sentiment;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** RatingRepository(out 포트)의 메서드를 Spring Data가 구현한다. 서비스는 RatingRepository만 알고 이 인터페이스는 모른다. */
public interface JpaRatingRepository extends JpaRepository<Rating, Long>, RatingRepository {

    /**
     * (content_id, user_id) 유니크 제약에 걸리면 넣지 않는다. 충돌 대상을 적지 않은 형태는 H2도 지원한다.
     * 네이티브 INSERT라 JPA Auditing이 동작하지 않아서 created_at·updated_at을 직접 채운다.
     */
    @Override
    @Modifying
    @Query(value = """
            INSERT INTO ratings (content_id, user_id, sentiment, score, title, body, progress, created_at, updated_at)
            VALUES (:contentId, :userId, :sentiment, :score, :title, :body, :progress,
                    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("contentId") Long contentId, @Param("userId") Long userId,
                       @Param("sentiment") String sentiment, @Param("score") int score, @Param("title") String title,
                       @Param("body") String body, @Param("progress") Integer progress);

    @Override
    @Query("""
            select new com.trendizm.backend.rating.application.port.out.SentimentCount(
                r.contentId, r.sentiment, count(r), avg(r.score))
            from Rating r
            where r.contentId in :contentIds
            group by r.contentId, r.sentiment
            """)
    List<SentimentCount> countGroupByContentIdAndSentiment(@Param("contentIds") Collection<Long> contentIds);

    @Override
    @Query(value = """
            select r from Rating r
            where r.contentId = :contentId
            order by (select count(l) from RatingLike l where l.ratingId = r.id) desc, r.id desc
            """,
            countQuery = "select count(r) from Rating r where r.contentId = :contentId")
    Page<Rating> findByContentIdOrderByLikes(@Param("contentId") Long contentId, Pageable pageable);

    @Override
    @Query(value = """
            select r from Rating r
            where r.contentId = :contentId and r.sentiment = :sentiment
            order by (select count(l) from RatingLike l where l.ratingId = r.id) desc, r.id desc
            """,
            countQuery = "select count(r) from Rating r where r.contentId = :contentId and r.sentiment = :sentiment")
    Page<Rating> findByContentIdAndSentimentOrderByLikes(@Param("contentId") Long contentId,
                                                         @Param("sentiment") Sentiment sentiment, Pageable pageable);
}
