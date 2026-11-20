package com.trendizm.backend.rating.application.port;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * 후기 목록 정렬. in 포트가 domain을 참조하지 않도록 application.port에 둔다.
 * 같은 값이면 최신 후기가 먼저 온다. API에서는 slug(예: score-high)로 받는다.
 */
public enum ReviewSort {

    /** 최신순 */
    LATEST,
    /** 별점 높은 순 */
    SCORE_HIGH,
    /** 별점 낮은 순 */
    SCORE_LOW;

    /** 예: SCORE_HIGH → score-high */
    public String getSlug() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    /** slug로 찾는다. 예: "score-low" → SCORE_LOW. 없으면 빈 Optional */
    public static Optional<ReviewSort> fromSlug(String slug) {
        return Arrays.stream(values()).filter(value -> value.getSlug().equals(slug)).findFirst();
    }
}
