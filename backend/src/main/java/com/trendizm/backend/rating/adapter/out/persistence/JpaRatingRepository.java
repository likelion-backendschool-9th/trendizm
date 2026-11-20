package com.trendizm.backend.rating.adapter.out.persistence;

import com.trendizm.backend.rating.application.port.out.RatingRepository;
import com.trendizm.backend.rating.application.port.out.SentimentCount;
import com.trendizm.backend.rating.domain.entity.Rating;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** RatingRepository(out 포트)의 메서드를 Spring Data가 구현한다. 서비스는 RatingRepository만 알고 이 인터페이스는 모른다. */
public interface JpaRatingRepository extends JpaRepository<Rating, Long>, RatingRepository {

    @Override
    @Query("""
            select new com.trendizm.backend.rating.application.port.out.SentimentCount(
                r.contentId, r.sentiment, count(r), avg(r.score))
            from Rating r
            where r.contentId in :contentIds
            group by r.contentId, r.sentiment
            """)
    List<SentimentCount> countGroupByContentIdAndSentiment(@Param("contentIds") Collection<Long> contentIds);
}
