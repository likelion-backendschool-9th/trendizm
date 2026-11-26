package com.trendizm.backend.ranking.application.port.out;

import com.trendizm.backend.ranking.domain.entity.TrendRanking;
import com.trendizm.backend.ranking.domain.enums.Platform;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 구현체는 adapter/out/persistence/JpaRankingRepository (Spring Data가 자동 구현).
 * 메서드 이름을 Spring Data 규칙(findByXxx 등)대로 추가하면 쿼리를 따로 쓰지 않아도 된다.
 */
public interface RankingRepository {

    /** 바로 DB에 반영한다(IDENTITY). 스냅샷 안에서 순위가 겹치면 여기서 DataIntegrityViolationException이 난다 */
    TrendRanking save(TrendRanking ranking);

    Optional<TrendRanking> findById(Long id);

    /** 가장 최근 스냅샷의 순위 하나. 이 순위의 collectedAt으로 그 스냅샷을 찾는다 */
    Optional<TrendRanking> findFirstByPlatformAndCategoryIdOrderByCollectedAtDesc(Platform platform, Long categoryId);

    /** 한 스냅샷의 1~10위. 저장은 전부 하고 조회 응답에는 10위까지만 준다 */
    List<TrendRanking> findTop10ByPlatformAndCategoryIdAndCollectedAtOrderByRankAsc(
            Platform platform, Long categoryId, LocalDateTime collectedAt);

    /** 같은 스냅샷(플랫폼·카테고리·수집 시각)이 이미 있으면 true. 같은 수집 결과를 다시 올리는지 확인한다 */
    boolean existsByPlatformAndCategoryIdAndCollectedAt(Platform platform, Long categoryId, LocalDateTime collectedAt);
}
