package com.trendizm.backend.ranking.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.ranking.application.dto.RankingCategoryResponse;
import com.trendizm.backend.ranking.application.dto.RankingLinkResponse;
import com.trendizm.backend.ranking.application.dto.RankingResponse;
import com.trendizm.backend.ranking.application.dto.RankingSnapshot;
import com.trendizm.backend.ranking.application.dto.RankingTabs;
import com.trendizm.backend.ranking.application.dto.RankingTabs.PlatformTab;
import com.trendizm.backend.ranking.application.exception.RankingErrorCode;
import com.trendizm.backend.ranking.application.port.RankingService;
import com.trendizm.backend.ranking.application.port.out.RankingTabRepository;
import com.trendizm.backend.ranking.application.port.out.RankingLinkRepository;
import com.trendizm.backend.ranking.application.port.out.RankingRepository;
import com.trendizm.backend.ranking.domain.entity.TrendRanking;
import com.trendizm.backend.ranking.domain.entity.TrendRankingCategory;
import com.trendizm.backend.ranking.domain.entity.TrendRankingLink;
import com.trendizm.backend.ranking.domain.enums.Platform;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class RankingServiceTest {

    private static final LocalDateTime OLD_COLLECTED_AT = LocalDateTime.of(2026, 11, 17, 9, 0);
    private static final LocalDateTime LATEST_COLLECTED_AT = LocalDateTime.of(2026, 11, 24, 9, 0);
    private static final LocalDateTime LATEST_STARTED_AT = LATEST_COLLECTED_AT.minusDays(7);

    @Autowired
    private RankingService rankingService;

    @Autowired
    private RankingRepository rankingRepository;

    @Autowired
    private RankingTabRepository tabRepository;

    @Autowired
    private RankingLinkRepository linkRepository;

    private Long sportsId;
    private Long foodId;
    private Long uncategorizedId;
    private TrendRanking latestFirst;
    private TrendRanking latestSecond;

    /**
     * 카테고리 sports, food, uncategorized(미분류)(저장 순서대로 id).
     * youtube·food에는 예전 스냅샷(1개 순위)과 최근 스냅샷(2개 순위)이 있고, x·food와 youtube·sports에도 순위가 하나씩 있다.
     * 최근 스냅샷 1위에는 링크 4개를 sortOrder가 섞인 순서로 저장한다. 미분류에도 순위가 하나 있다. 테스트가 끝나면 롤백된다
     */
    @BeforeEach
    void setUp() {
        sportsId = tabRepository.save(TrendRankingCategory.create("sports", "운동/스포츠")).getId();
        foodId = tabRepository.save(TrendRankingCategory.create("food", "맛집/음식")).getId();
        uncategorizedId = tabRepository.save(TrendRankingCategory.create("uncategorized", "미분류")).getId();
        rankingRepository.save(TrendRanking.create(uncategorizedId, Platform.youtube, 1, "미분류 키워드", null,
                LATEST_COLLECTED_AT));

        rankingRepository.save(TrendRanking.create(foodId, Platform.youtube, 1, "예전 키워드", null, OLD_COLLECTED_AT));
        // 2위를 먼저 저장해서 저장 순서가 아니라 순위 순으로 주는지 본다
        latestSecond = rankingRepository.save(
                TrendRanking.create(foodId, Platform.youtube, 2, "탕후루", LATEST_STARTED_AT, LATEST_COLLECTED_AT));
        latestFirst = rankingRepository.save(
                TrendRanking.create(foodId, Platform.youtube, 1, "두쫀쿠", LATEST_STARTED_AT, LATEST_COLLECTED_AT));
        latestFirst.changeSummary("두바이 쫀득 쿠키 줄임말");
        rankingRepository.save(TrendRanking.create(foodId, Platform.x, 1, "다른 플랫폼", null, LATEST_COLLECTED_AT));
        rankingRepository.save(TrendRanking.create(sportsId, Platform.youtube, 1, "다른 카테고리", null, LATEST_COLLECTED_AT));

        for (int sortOrder : new int[] {2, 0, 3, 1}) {
            linkRepository.save(TrendRankingLink.create(latestFirst.getId(), "링크 " + sortOrder,
                    "https://youtu.be/" + sortOrder, null, null, null, sortOrder));
        }
    }

    @Test
    @DisplayName("가장 최근 스냅샷의 순위를 순위 순으로 주고, 집계 기간과 수집 시각을 함께 준다")
    void 최근_스냅샷() {
        RankingSnapshot snapshot = rankingService.getLatestSnapshot("youtube", "food");

        assertThat(snapshot.platform()).isEqualTo("youtube");
        assertThat(snapshot.categoryId()).isEqualTo(foodId);
        assertThat(snapshot.startedAt()).isEqualTo(LATEST_STARTED_AT);
        assertThat(snapshot.collectedAt()).isEqualTo(LATEST_COLLECTED_AT);
        assertThat(snapshot.rankings()).extracting(RankingResponse::id)
                .containsExactly(latestFirst.getId(), latestSecond.getId());
        assertThat(snapshot.rankings()).extracting(RankingResponse::summary)
                .containsExactly("두바이 쫀득 쿠키 줄임말", null);
    }

    @Test
    @DisplayName("관련 링크는 순위마다 전부 sortOrder 순으로 주고, 링크가 없는 순위는 빈 목록이다")
    void 관련_링크() {
        RankingSnapshot snapshot = rankingService.getLatestSnapshot("youtube", "food");

        assertThat(snapshot.rankings().get(0).links()).extracting(RankingLinkResponse::title)
                .containsExactly("링크 0", "링크 1", "링크 2", "링크 3");
        assertThat(snapshot.rankings().get(1).links()).isEmpty();
    }

    @Test
    @DisplayName("집계 시작 시각이 없는 스냅샷이면 startedAt은 null이다")
    void 집계_시작_없음() {
        RankingSnapshot snapshot = rankingService.getLatestSnapshot("x", "food");

        assertThat(snapshot.startedAt()).isNull();
        assertThat(snapshot.rankings()).extracting(RankingResponse::keyword).containsExactly("다른 플랫폼");
    }

    @Test
    @DisplayName("아직 수집한 스냅샷이 없으면 시각은 null이고 순위는 빈 목록이다")
    void 스냅샷_없음() {
        RankingSnapshot snapshot = rankingService.getLatestSnapshot("tiktok", "food");

        assertThat(snapshot.platform()).isEqualTo("tiktok");
        assertThat(snapshot.categoryId()).isEqualTo(foodId);
        assertThat(snapshot.startedAt()).isNull();
        assertThat(snapshot.collectedAt()).isNull();
        assertThat(snapshot.rankings()).isEmpty();
    }

    @Test
    @DisplayName("없는 플랫폼이면 RANKING_PLATFORM_NOT_FOUND (대문자 slug도 없는 값)")
    void 없는_플랫폼() {
        assertThatThrownBy(() -> rankingService.getLatestSnapshot("naver", "food"))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(RankingErrorCode.RANKING_PLATFORM_NOT_FOUND));
        assertThatThrownBy(() -> rankingService.getLatestSnapshot("YOUTUBE", "food"))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(RankingErrorCode.RANKING_PLATFORM_NOT_FOUND));
    }

    @Test
    @DisplayName("없는 카테고리 slug면 RANKING_CATEGORY_NOT_FOUND (대문자 slug, 카테고리 id도 없는 값)")
    void 없는_카테고리() {
        for (String category : new String[] {"nope", "FOOD", String.valueOf(foodId)}) {
            assertThatThrownBy(() -> rankingService.getLatestSnapshot("youtube", category))
                    .isInstanceOfSatisfying(BusinessException.class, e ->
                            assertThat(e.getErrorCode()).isEqualTo(RankingErrorCode.RANKING_CATEGORY_NOT_FOUND));
        }
    }

    @Test
    @DisplayName("미분류 카테고리는 조회 응답에서 빼므로 없는 카테고리와 같이 RANKING_CATEGORY_NOT_FOUND")
    void 미분류_순위() {
        assertThatThrownBy(() -> rankingService.getLatestSnapshot("youtube", "uncategorized"))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(RankingErrorCode.RANKING_CATEGORY_NOT_FOUND));
    }

    @Test
    @DisplayName("스냅샷에 순위가 10개보다 많으면 1~10위만 준다")
    void 상위_10위() {
        LocalDateTime collectedAt = LATEST_COLLECTED_AT.plusHours(1);
        for (int rank = 12; rank >= 1; rank--) {
            rankingRepository.save(TrendRanking.create(sportsId, Platform.tiktok, rank, "키워드 " + rank, null, collectedAt));
        }

        RankingSnapshot snapshot = rankingService.getLatestSnapshot("tiktok", "sports");

        assertThat(snapshot.rankings()).extracting(RankingResponse::rank)
                .containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    @DisplayName("탭 목록: 플랫폼은 enum 선언 순서로 화면 이름(label)과 함께, 카테고리는 미분류를 빼고 id 순이다")
    void 탭_목록() {
        RankingTabs tabs = rankingService.getTabs();

        assertThat(tabs.platforms())
                .extracting(PlatformTab::slug, PlatformTab::label)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("youtube", "YouTube"),
                        org.assertj.core.groups.Tuple.tuple("instagram", "Instagram"),
                        org.assertj.core.groups.Tuple.tuple("threads", "Threads"),
                        org.assertj.core.groups.Tuple.tuple("x", "X"),
                        org.assertj.core.groups.Tuple.tuple("tiktok", "TikTok"));
        assertThat(tabs.categories())
                .extracting(RankingCategoryResponse::id, RankingCategoryResponse::slug, RankingCategoryResponse::title)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(sportsId, "sports", "운동/스포츠"),
                        org.assertj.core.groups.Tuple.tuple(foodId, "food", "맛집/음식"));
    }
}
