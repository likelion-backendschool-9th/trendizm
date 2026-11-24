package com.trendizm.backend.ranking.application.port.out;

import com.trendizm.backend.ranking.domain.entity.TrendRankingLink;
import java.util.Collection;
import java.util.List;

/** 구현체는 adapter/out/persistence/JpaRankingLinkRepository (Spring Data가 자동 구현) */
public interface RankingLinkRepository {

    TrendRankingLink save(TrendRankingLink link);

    /** 여러 순위의 링크를 한 번에 가져온다. 한 순위 안에서는 sortOrder 순이다 */
    List<TrendRankingLink> findByRankingIdInOrderBySortOrderAscIdAsc(Collection<Long> rankingIds);
}
