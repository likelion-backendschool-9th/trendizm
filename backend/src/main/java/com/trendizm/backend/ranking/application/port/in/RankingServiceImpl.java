package com.trendizm.backend.ranking.application.port.in;

import com.trendizm.backend.ranking.application.dto.RankingResponse;
import com.trendizm.backend.ranking.application.port.RankingService;
import com.trendizm.backend.ranking.application.port.out.RankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingServiceImpl implements RankingService {

    private final RankingRepository rankingRepository;

    @Override
    public Page<RankingResponse> getRankings(Pageable pageable) {
        // TODO(ranking): 순위 목록 조회
        //  1. rankingRepository.findAll(pageable)
        //  2. .map(RankingResponse::from) 으로 변환해 반환
        //  (숏폼·롱폼 탭, 해시태그 조회는 컬럼이 정해지면 RankingRepository에 findByXxx를 추가)
        throw new UnsupportedOperationException("TODO(ranking): 순위 목록 조회");
    }

    @Override
    public RankingResponse getRanking(Long rankingId) {
        // TODO(ranking): 순위 단건 조회
        //  1. rankingRepository.findById(rankingId), 없으면 예외를 던진다
        //  2. RankingResponse.from(ranking) 반환
        throw new UnsupportedOperationException("TODO(ranking): 순위 단건 조회");
    }

    @Override
    @Transactional
    public Long createRanking() {
        // TODO(ranking): 순위 등록
        //  1. Ranking.create(...) (영상 링크 → 임베드 URL 변환은 여기서)
        //  2. rankingRepository.save(ranking).getId() 반환
        throw new UnsupportedOperationException("TODO(ranking): 순위 등록");
    }

    @Override
    @Transactional
    public void updateRanking(Long rankingId) {
        // TODO(ranking): 순위 수정
        //  1. rankingRepository.findById(rankingId), 없으면 NOT_FOUND
        //  2. Ranking에 update(...) 메서드를 만들어 호출 (트랜잭션 안이라 변경 감지로 저장된다)
        throw new UnsupportedOperationException("TODO(ranking): 순위 수정");
    }

    @Override
    @Transactional
    public void deleteRanking(Long rankingId) {
        // TODO(ranking): 순위 삭제
        //  1. rankingRepository.findById(rankingId), 없으면 NOT_FOUND
        //  2. rankingRepository.delete(ranking)
        throw new UnsupportedOperationException("TODO(ranking): 순위 삭제");
    }
}
