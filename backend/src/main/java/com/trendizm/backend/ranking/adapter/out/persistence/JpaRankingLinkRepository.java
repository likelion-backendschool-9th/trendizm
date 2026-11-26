package com.trendizm.backend.ranking.adapter.out.persistence;

import com.trendizm.backend.ranking.application.port.out.RankingLinkRepository;
import com.trendizm.backend.ranking.domain.entity.TrendRankingLink;
import org.springframework.data.jpa.repository.JpaRepository;

/** RankingLinkRepository(out 포트)의 메서드를 Spring Data가 구현한다. 서비스는 RankingLinkRepository만 안다. */
public interface JpaRankingLinkRepository extends JpaRepository<TrendRankingLink, Long>, RankingLinkRepository {
}
