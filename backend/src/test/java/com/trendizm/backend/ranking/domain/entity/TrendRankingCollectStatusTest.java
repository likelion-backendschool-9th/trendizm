package com.trendizm.backend.ranking.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.trendizm.backend.ranking.domain.enums.Platform;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TrendRankingCollectStatusTest {

    @Test
    @DisplayName("처음에는 수집하지 않는 상태이고, 켜고 끌 수 있다")
    void 수집_여부() {
        TrendRankingCollectStatus collectStatus = TrendRankingCollectStatus.create(Platform.youtube, 50, "viewCount", 2);
        assertThat(collectStatus.isStatus()).isFalse();

        collectStatus.changeStatus(true);
        assertThat(collectStatus.isStatus()).isTrue();

        collectStatus.changeStatus(false);
        assertThat(collectStatus.isStatus()).isFalse();
    }
}
