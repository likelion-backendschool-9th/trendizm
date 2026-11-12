package com.trendizm.backend.ranking.application.port;

import com.trendizm.backend.ranking.application.dto.RankingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** 컬럼이 정해지면 createRanking·updateRanking에 파라미터를 추가한다. */
public interface RankingService {

    Page<RankingResponse> getRankings(Pageable pageable);

    /** 없으면 예외를 던진다 */
    RankingResponse getRanking(Long rankingId);

    /** @return 생성한 순위 id */
    Long createRanking();

    void updateRanking(Long rankingId);

    void deleteRanking(Long rankingId);
}
