package com.trendizm.backend.rating.domain.enums;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/** 후기의 호/불호 (ERD ratings.sentiment, varchar(30)) */
public enum Sentiment {

    LIKE("좋아요"),
    DISLIKE("싫어요");

    private final String label;

    Sentiment(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** API 요청과 화면 주소에 쓰는 값. 예: LIKE → like (/rating/{id}/like) */
    public String getSlug() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** slug로 찾는다. 예: "dislike" → DISLIKE. 없으면 빈 Optional */
    public static Optional<Sentiment> fromSlug(String slug) {
        return Arrays.stream(values()).filter(value -> value.getSlug().equals(slug)).findFirst();
    }
}
