package com.trendizm.backend.ranking.application.port.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.ranking.application.dto.RankingImportRow;
import com.trendizm.backend.ranking.application.exception.RankingErrorCode;
import com.trendizm.backend.ranking.application.port.out.RankingTabRepository;
import com.trendizm.backend.ranking.application.port.out.RankingLinkRepository;
import com.trendizm.backend.ranking.application.port.out.RankingRepository;
import com.trendizm.backend.ranking.domain.entity.TrendRankingCategory;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 순위 CSV를 저장하다 DB 제약에 걸린 경우. H2에서는 엔티티와 같은 테이블이 만들어져 이 상황을 재현하기 어려워서
 * 저장소가 예외를 던지게 한다. 실제로는 Hibernate 예외를 거쳐 JDBC 예외(SQLState)가 원인으로 들어 있다.
 */
@DisplayName("순위 CSV 저장 중 DB 제약 위반")
class RankingImportSaveFailureTest {

    private final RankingRepository rankingRepository = mock(RankingRepository.class);
    private final RankingTabRepository tabRepository = mock(RankingTabRepository.class);
    private final RankingLinkRepository linkRepository = mock(RankingLinkRepository.class);
    private final RankingServiceImpl rankingService =
            new RankingServiceImpl(rankingRepository, tabRepository, linkRepository);

    @BeforeEach
    void setUp() {
        TrendRankingCategory challenge = TrendRankingCategory.create("challenge", "챌린지");
        ReflectionTestUtils.setField(challenge, "id", 1L);
        given(tabRepository.findByTitleIn(any())).willReturn(List.of(challenge));
        // 실제 저장처럼 id를 채워 돌려준다(관련 링크가 이 id를 쓴다)
        given(rankingRepository.save(any())).willAnswer(invocation -> {
            Object ranking = invocation.getArgument(0);
            ReflectionTestUtils.setField(ranking, "id", 10L);
            return ranking;
        });
    }

    @Test
    @DisplayName("UNIQUE 위반(SQLState 23505)은 같은 스냅샷을 거의 동시에 올린 것이라 409 RANKING_SNAPSHOT_ALREADY_EXISTS")
    void UNIQUE_위반은_중복_업로드() {
        willThrow(violation("23505")).given(rankingRepository).save(any());

        assertThatThrownBy(() -> rankingService.importRankings(List.of(row())))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(RankingErrorCode.RANKING_SNAPSHOT_ALREADY_EXISTS));
    }

    @Test
    @DisplayName("NOT NULL 위반(SQLState 23502) 같은 다른 제약 위반은 중복 업로드로 바꾸지 않고 그대로 던진다")
    void 다른_제약_위반은_그대로() {
        given(linkRepository.save(any())).willThrow(violation("23502"));

        assertThatThrownBy(() -> rankingService.importRankings(List.of(row())))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    /** Spring 예외 → Hibernate 예외 → JDBC 예외 순서로 감싼 모양 */
    private static DataIntegrityViolationException violation(String sqlState) {
        SQLException jdbc = new SQLException("constraint violation", sqlState);
        return new DataIntegrityViolationException("could not execute statement",
                new RuntimeException("hibernate", jdbc));
    }

    private static RankingImportRow row() {
        return new RankingImportRow(2, "instagram", "챌린지", "1", "곶감챌린지",
                List.of("CollectedA1"), List.of(), List.of(), "2026-11-23 12:16", null);
    }
}
