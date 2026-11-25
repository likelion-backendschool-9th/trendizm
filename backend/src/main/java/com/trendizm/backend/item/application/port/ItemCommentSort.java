package com.trendizm.backend.item.application.port;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * 댓글 목록 정렬. in 포트가 domain을 참조하지 않도록 application.port에 둔다.
 * 같은 값이면 최신 댓글이 먼저 온다. API에서는 slug(예: likes)로 받는다.
 */
public enum ItemCommentSort {
    /** 최신순 */
    LATEST,
    /** 공감 많은 순 (대댓글은 정렬과 상관없이 댓글 아래에 오래된 순) */
    LIKES;

    /** 예: LATEST → latest */
    public String getSlug() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    /** slug로 찾는다. 예: "likes" → LIKES. 없으면 빈 Optional */
    public static Optional<ItemCommentSort> fromSlug(String slug) {
        return Arrays.stream(values()).filter(value -> value.getSlug().equals(slug)).findFirst();
    }
}
