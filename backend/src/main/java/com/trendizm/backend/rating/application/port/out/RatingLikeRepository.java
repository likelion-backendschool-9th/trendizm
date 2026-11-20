package com.trendizm.backend.rating.application.port.out;

import com.trendizm.backend.rating.domain.entity.RatingLike;

/** 후기 공감(rating_likes) 저장소. 구현체는 adapter/out/persistence/JpaRatingLikeRepository */
public interface RatingLikeRepository {

    RatingLike save(RatingLike like);
}
