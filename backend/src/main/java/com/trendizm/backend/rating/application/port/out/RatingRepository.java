package com.trendizm.backend.rating.application.port.out;

import com.trendizm.backend.rating.domain.entity.Rating;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 후기(ratings) 저장소. 구현체는 adapter/out/persistence/JpaRatingRepository (Spring Data가 자동 구현).
 * 메서드 이름을 Spring Data 규칙(findByXxx 등)대로 추가하면 쿼리를 따로 쓰지 않아도 된다.
 */
public interface RatingRepository {

    Rating save(Rating rating);

    Optional<Rating> findById(Long id);

    /** 콘텐츠·호불호별 후기 수와 평균 별점. 후기가 없는 조합은 결과에 없다 */
    List<SentimentCount> countGroupByContentIdAndSentiment(Collection<Long> contentIds);
}
