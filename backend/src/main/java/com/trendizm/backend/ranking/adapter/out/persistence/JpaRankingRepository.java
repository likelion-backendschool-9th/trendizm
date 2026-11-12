package com.trendizm.backend.ranking.adapter.out.persistence;

import com.trendizm.backend.ranking.application.port.out.RankingRepository;
import com.trendizm.backend.ranking.domain.Ranking;
import org.springframework.data.jpa.repository.JpaRepository;

/** RankingRepository(out 포트)의 메서드를 Spring Data가 구현한다. 서비스는 RankingRepository만 알고 이 인터페이스는 모른다. */
public interface JpaRankingRepository extends JpaRepository<Ranking, Long>, RankingRepository {
}
