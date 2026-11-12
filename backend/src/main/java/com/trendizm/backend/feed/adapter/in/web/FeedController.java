package com.trendizm.backend.feed.adapter.in.web;

import com.trendizm.backend.feed.adapter.in.web.dto.FeedRequest;
import com.trendizm.backend.feed.application.dto.FeedResponse;
import com.trendizm.backend.feed.application.port.FeedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 로그인 없이 모든 요청 허용 (auth/adapter/out/security/SecurityConfig) */
@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    public Page<FeedResponse> getFeeds(Pageable pageable) {
        return feedService.getFeeds(pageable);
    }

    @GetMapping("/{feedId}")
    public FeedResponse getFeed(@PathVariable Long feedId) {
        return feedService.getFeed(feedId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long createFeed(@Valid @RequestBody FeedRequest request) {
        return feedService.createFeed();
    }

    @PutMapping("/{feedId}")
    public void updateFeed(@PathVariable Long feedId, @Valid @RequestBody FeedRequest request) {
        feedService.updateFeed(feedId);
    }

    @DeleteMapping("/{feedId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFeed(@PathVariable Long feedId) {
        feedService.deleteFeed(feedId);
    }
}
