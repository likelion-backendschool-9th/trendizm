package com.trendizm.backend.rating.adapter.out.persistence;

import com.trendizm.backend.rating.application.port.out.RatingContentRepository;
import com.trendizm.backend.rating.domain.entity.RatingContent;
import org.springframework.data.jpa.repository.JpaRepository;

/** RatingContentRepository(out 포트)의 메서드를 Spring Data가 구현한다 */
public interface JpaRatingContentRepository extends JpaRepository<RatingContent, Long>, RatingContentRepository {
}
