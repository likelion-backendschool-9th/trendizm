package com.trendizm.backend.ranking.adapter.out.persistence;

import com.trendizm.backend.ranking.application.port.out.RankingTabRepository;
import com.trendizm.backend.ranking.domain.entity.TrendRankingCategory;
import org.springframework.data.jpa.repository.JpaRepository;

/** RankingTabRepository(out 포트)의 메서드를 Spring Data가 구현한다. 서비스는 RankingTabRepository만 안다. */
public interface JpaRankingTabRepository
        extends JpaRepository<TrendRankingCategory, Long>, RankingTabRepository {
}
