package com.trendizm.backend.feed.application.dto;

import com.trendizm.backend.feed.domain.Feed;

/** 컬럼이 정해지면 응답 필드를 추가한다. */
public record FeedResponse(Long id) {

    public static FeedResponse from(Feed feed) {
        return new FeedResponse(feed.getId());
    }
}
