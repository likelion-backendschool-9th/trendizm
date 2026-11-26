package com.trendizm.backend.ranking.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.trendizm.backend.ranking.domain.enums.Platform;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TrendRankingTest {

    @Test
    @DisplayName("집계 시작 시각과 수집 시각은 DB처럼 마이크로초까지만 남긴다")
    void 시각_마이크로초() {
        LocalDateTime startedAt = LocalDateTime.of(2026, 11, 16, 9, 0, 0, 123_456_789);
        LocalDateTime collectedAt = LocalDateTime.of(2026, 11, 23, 9, 5, 0, 987_654_321);

        TrendRanking ranking = TrendRanking.create(1L, Platform.youtube, 1, "키워드", startedAt, collectedAt);

        assertThat(ranking.getStartedAt()).isEqualTo(LocalDateTime.of(2026, 11, 16, 9, 0, 0, 123_456_000));
        assertThat(ranking.getCollectedAt()).isEqualTo(LocalDateTime.of(2026, 11, 23, 9, 5, 0, 987_654_000));
    }

    @Test
    @DisplayName("집계 시작 시각은 비어 있을 수 있다")
    void 시작_시각_없음() {
        TrendRanking ranking = TrendRanking.create(1L, Platform.youtube, 1, "키워드", null, LocalDateTime.now());

        assertThat(ranking.getStartedAt()).isNull();
    }

    @Test
    @DisplayName("처음에는 AI 요약이 없고, 생성하면 저장하고 다시 생성하면 덮어쓴다")
    void AI_요약() {
        TrendRanking ranking = TrendRanking.create(1L, Platform.youtube, 1, "키워드", null, LocalDateTime.now());
        assertThat(ranking.getSummary()).isNull();

        ranking.changeSummary("첫 요약");
        ranking.changeSummary("새 요약");

        assertThat(ranking.getSummary()).isEqualTo("새 요약");
    }
}
