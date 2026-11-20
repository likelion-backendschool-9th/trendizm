package com.trendizm.backend.rating.application.dto;

import com.trendizm.backend.rating.domain.enums.Sentiment;

/** 응답에 넣는 호/불호. slug는 API 요청값(like·dislike), label은 화면 이름(좋아요·싫어요) */
public record SentimentInfo(String slug, String label) {

    public static SentimentInfo of(Sentiment sentiment) {
        return new SentimentInfo(sentiment.getSlug(), sentiment.getLabel());
    }
}
