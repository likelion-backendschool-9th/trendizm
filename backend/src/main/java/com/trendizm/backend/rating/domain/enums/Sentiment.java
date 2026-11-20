package com.trendizm.backend.rating.domain.enums;

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
}
