package com.trendizm.backend.ranking.application.port.out;

import com.trendizm.backend.ranking.domain.entity.TrendRankingCategory;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 순위 탭 중 카테고리(trend_ranking_categories)를 다룬다. 플랫폼 탭은 Platform enum이라 저장하지 않는다.
 * 구현체는 adapter/out/persistence/JpaRankingTabRepository (Spring Data가 자동 구현)
 */
public interface RankingTabRepository {

    TrendRankingCategory save(TrendRankingCategory category);

    Optional<TrendRankingCategory> findById(Long id);

    /** 순위 조회(GET /api/rankings)의 category 값(slug)으로 찾는다. 예: challenge */
    Optional<TrendRankingCategory> findBySlug(String slug);

    /** 순위 CSV의 category 값(한글 이름)으로 찾는다. 없는 이름은 결과에 빠진다 */
    List<TrendRankingCategory> findByTitleIn(Collection<String> titles);

    List<TrendRankingCategory> findAllByOrderByIdAsc();
}
