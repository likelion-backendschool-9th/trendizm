package com.trendizm.backend.rating.application.port;

import com.trendizm.backend.rating.application.dto.RatingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** 컬럼이 정해지면 createRating·updateRating에 파라미터를 추가한다. */
public interface RatingService {

    Page<RatingResponse> getRatings(Pageable pageable);

    /** 없으면 예외를 던진다 */
    RatingResponse getRating(Long ratingId);

    /** @return 생성한 평가 id */
    Long createRating();

    void updateRating(Long ratingId);

    void deleteRating(Long ratingId);
}
