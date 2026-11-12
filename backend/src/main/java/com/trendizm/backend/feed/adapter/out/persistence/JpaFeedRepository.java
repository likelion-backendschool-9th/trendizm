package com.trendizm.backend.feed.adapter.out.persistence;

import com.trendizm.backend.feed.application.port.out.FeedRepository;
import com.trendizm.backend.feed.domain.Feed;
import org.springframework.data.jpa.repository.JpaRepository;

/** FeedRepository(out 포트)의 메서드를 Spring Data가 구현한다. 서비스는 FeedRepository만 알고 이 인터페이스는 모른다. */
public interface JpaFeedRepository extends JpaRepository<Feed, Long>, FeedRepository {
}
