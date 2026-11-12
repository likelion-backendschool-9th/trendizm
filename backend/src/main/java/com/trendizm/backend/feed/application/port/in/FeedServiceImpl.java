package com.trendizm.backend.feed.application.port.in;

import com.trendizm.backend.feed.application.dto.FeedResponse;
import com.trendizm.backend.feed.application.port.FeedService;
import com.trendizm.backend.feed.application.port.out.FeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedServiceImpl implements FeedService {

    private final FeedRepository feedRepository;

    @Override
    public Page<FeedResponse> getFeeds(Pageable pageable) {
        // TODO(feed): 피드 목록 조회
        //  1. feedRepository.findAll(pageable)
        //  2. .map(FeedResponse::from) 으로 변환해 반환
        //  (숏폼·롱폼 탭, 해시태그 조회는 컬럼이 정해지면 FeedRepository에 findByXxx를 추가)
        throw new UnsupportedOperationException("TODO(feed): 피드 목록 조회");
    }

    @Override
    public FeedResponse getFeed(Long feedId) {
        // TODO(feed): 피드 단건 조회
        //  1. feedRepository.findById(feedId), 없으면 예외를 던진다
        //  2. FeedResponse.from(feed) 반환
        throw new UnsupportedOperationException("TODO(feed): 피드 단건 조회");
    }

    @Override
    @Transactional
    public Long createFeed() {
        // TODO(feed): 피드 등록
        //  1. Feed.create(...) (영상 링크 → 임베드 URL 변환은 여기서)
        //  2. feedRepository.save(feed).getId() 반환
        throw new UnsupportedOperationException("TODO(feed): 피드 등록");
    }

    @Override
    @Transactional
    public void updateFeed(Long feedId) {
        // TODO(feed): 피드 수정
        //  1. feedRepository.findById(feedId), 없으면 NOT_FOUND
        //  2. Feed에 update(...) 메서드를 만들어 호출 (트랜잭션 안이라 변경 감지로 저장된다)
        throw new UnsupportedOperationException("TODO(feed): 피드 수정");
    }

    @Override
    @Transactional
    public void deleteFeed(Long feedId) {
        // TODO(feed): 피드 삭제
        //  1. feedRepository.findById(feedId), 없으면 NOT_FOUND
        //  2. feedRepository.delete(feed)
        throw new UnsupportedOperationException("TODO(feed): 피드 삭제");
    }
}
