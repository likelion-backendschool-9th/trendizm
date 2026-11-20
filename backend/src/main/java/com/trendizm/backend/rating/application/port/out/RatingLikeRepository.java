package com.trendizm.backend.rating.application.port.out;

import com.trendizm.backend.rating.domain.entity.RatingLike;
import java.util.Collection;
import java.util.List;

/** 후기 공감(rating_likes) 저장소. 구현체는 adapter/out/persistence/JpaRatingLikeRepository */
public interface RatingLikeRepository {

    RatingLike save(RatingLike like);

    /**
     * 공감을 추가한다. 이미 있으면 아무것도 하지 않는다(동시에 눌러도 한 줄만 생긴다).
     * @return 추가한 행 수 (0 또는 1)
     */
    int insertIfAbsent(Long ratingId, Long userId);

    /** @return 지운 행 수 */
    int deleteByRatingIdAndUserId(Long ratingId, Long userId);

    /** 후기 id별 공감 수. 공감이 없는 후기는 결과에 없다 */
    List<ReviewLikeCount> countGroupByRatingId(Collection<Long> ratingIds);

    /** 이 회원이 공감한 후기 id (주어진 후기 중에서) */
    List<Long> findLikedRatingIds(Long userId, Collection<Long> ratingIds);
}
