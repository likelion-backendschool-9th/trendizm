package com.trendizm.backend.rating.application.port.in;

import com.trendizm.backend.rating.application.dto.RatingResponse;
import com.trendizm.backend.rating.application.port.RatingService;
import com.trendizm.backend.rating.application.port.out.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;

    @Override
    public Page<RatingResponse> getRatings(Pageable pageable) {
        // TODO(rating): 평가 목록 조회
        //  1. ratingRepository.findAll(pageable)
        //  2. .map(RatingResponse::from) 으로 변환해 반환
        //  (숏폼·롱폼 탭, 해시태그 조회는 컬럼이 정해지면 RatingRepository에 findByXxx를 추가)
        throw new UnsupportedOperationException("TODO(rating): 평가 목록 조회");
    }

    @Override
    public RatingResponse getRating(Long ratingId) {
        // TODO(rating): 평가 단건 조회
        //  1. ratingRepository.findById(ratingId), 없으면 예외를 던진다
        //  2. RatingResponse.from(rating) 반환
        throw new UnsupportedOperationException("TODO(rating): 평가 단건 조회");
    }

    @Override
    @Transactional
    public Long createRating() {
        // TODO(rating): 평가 등록
        //  1. Rating.create(...) (영상 링크 → 임베드 URL 변환은 여기서)
        //  2. ratingRepository.save(rating).getId() 반환
        throw new UnsupportedOperationException("TODO(rating): 평가 등록");
    }

    @Override
    @Transactional
    public void updateRating(Long ratingId) {
        // TODO(rating): 평가 수정
        //  1. ratingRepository.findById(ratingId), 없으면 NOT_FOUND
        //  2. Rating에 update(...) 메서드를 만들어 호출 (트랜잭션 안이라 변경 감지로 저장된다)
        throw new UnsupportedOperationException("TODO(rating): 평가 수정");
    }

    @Override
    @Transactional
    public void deleteRating(Long ratingId) {
        // TODO(rating): 평가 삭제
        //  1. ratingRepository.findById(ratingId), 없으면 NOT_FOUND
        //  2. ratingRepository.delete(rating)
        throw new UnsupportedOperationException("TODO(rating): 평가 삭제");
    }
}
