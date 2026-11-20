package com.trendizm.backend.rating.application.port.out;

import com.trendizm.backend.rating.domain.entity.Rating;
import com.trendizm.backend.rating.domain.enums.Sentiment;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 후기(ratings) 저장소. 구현체는 adapter/out/persistence/JpaRatingRepository (Spring Data가 자동 구현).
 * 메서드 이름을 Spring Data 규칙(findByXxx 등)대로 추가하면 쿼리를 따로 쓰지 않아도 된다.
 */
public interface RatingRepository {

    Rating save(Rating rating);

    Optional<Rating> findById(Long id);

    Optional<Rating> findByContentIdAndUserId(Long contentId, Long userId);

    /** 바뀐 내용을 DB에 바로 반영한다. updated_at(@LastModifiedDate)은 이때 채워진다 (JpaRepository가 구현) */
    void flush();

    /**
     * 후기를 추가한다. (content_id, user_id)가 이미 있으면 아무것도 하지 않는다(동시에 처음 작성해도 한 줄만 생긴다).
     * 값 검사는 하지 않으므로 Rating.create로 먼저 검사한 값을 넘긴다.
     * @return 추가한 행 수 (0 또는 1)
     */
    int insertIfAbsent(Long contentId, Long userId, String sentiment, int score, String title, String body,
                       Integer progress);

    Page<Rating> findByContentId(Long contentId, Pageable pageable);

    Page<Rating> findByContentIdAndSentiment(Long contentId, Sentiment sentiment, Pageable pageable);

    /** 콘텐츠·호불호별 후기 수와 평균 별점. 후기가 없는 조합은 결과에 없다 */
    List<SentimentCount> countGroupByContentIdAndSentiment(Collection<Long> contentIds);
}
