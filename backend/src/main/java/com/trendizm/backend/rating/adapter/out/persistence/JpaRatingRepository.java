package com.trendizm.backend.rating.adapter.out.persistence;

import com.trendizm.backend.rating.application.port.out.RatingRepository;
import com.trendizm.backend.rating.domain.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

/** RatingRepository(out 포트)의 메서드를 Spring Data가 구현한다. 서비스는 RatingRepository만 알고 이 인터페이스는 모른다. */
public interface JpaRatingRepository extends JpaRepository<Rating, Long>, RatingRepository {
}
