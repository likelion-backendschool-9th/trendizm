package com.trendizm.backend.feed.application.port;

import com.trendizm.backend.feed.application.dto.FeedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** 컬럼이 정해지면 createFeed·updateFeed에 파라미터를 추가한다. */
public interface FeedService {

    Page<FeedResponse> getFeeds(Pageable pageable);

    /** 없으면 예외를 던진다 */
    FeedResponse getFeed(Long feedId);

    /** @return 생성한 피드 id */
    Long createFeed();

    void updateFeed(Long feedId);

    void deleteFeed(Long feedId);
}
