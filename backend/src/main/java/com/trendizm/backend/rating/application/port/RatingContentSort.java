package com.trendizm.backend.rating.application.port;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * 콘텐츠 목록 정렬. in 포트가 domain을 참조하지 않도록 application.port에 둔다.
 * 같은 값이면 최신 등록 콘텐츠가 먼저 온다. API에서는 slug(예: dislikes)로 받는다.
 */
public enum RatingContentSort {
    /** 최신 등록순 */
    LATEST,
    /** 후기 많은 순 */
    REVIEWS,
    /** 좋아요 후기 많은 순 */
    LIKES,
    /** 싫어요 후기 많은 순 */
    DISLIKES,
    /** 평균 별점 높은 순 (후기가 없는 콘텐츠는 맨 뒤) */
    SCORE;

    /** 예: DISLIKES → dislikes */
    public String getSlug() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    /** slug로 찾는다. 예: "score" → SCORE. 없으면 빈 Optional */
    public static Optional<RatingContentSort> fromSlug(String slug) {
        return Arrays.stream(values()).filter(value -> value.getSlug().equals(slug)).findFirst();
    }
}
