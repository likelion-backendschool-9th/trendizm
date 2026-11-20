package com.trendizm.backend.rating.application.port.out;

import com.trendizm.backend.rating.domain.entity.RatingContent;
import java.util.Optional;

/** 평점 콘텐츠(rating_contents) 저장소. 구현체는 adapter/out/persistence/JpaRatingContentRepository */
public interface RatingContentRepository {

    RatingContent save(RatingContent content);

    Optional<RatingContent> findById(Long id);
}
