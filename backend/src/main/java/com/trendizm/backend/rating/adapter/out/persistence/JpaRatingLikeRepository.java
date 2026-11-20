package com.trendizm.backend.rating.adapter.out.persistence;

import com.trendizm.backend.rating.application.port.out.RatingLikeRepository;
import com.trendizm.backend.rating.domain.entity.RatingLike;
import org.springframework.data.jpa.repository.JpaRepository;

/** RatingLikeRepository(out 포트)의 메서드를 Spring Data가 구현한다 */
public interface JpaRatingLikeRepository extends JpaRepository<RatingLike, Long>, RatingLikeRepository {
}
