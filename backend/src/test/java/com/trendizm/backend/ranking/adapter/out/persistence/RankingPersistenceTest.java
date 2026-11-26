package com.trendizm.backend.ranking.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.trendizm.backend.ranking.application.port.out.RankingTabRepository;
import com.trendizm.backend.ranking.application.port.out.RankingLinkRepository;
import com.trendizm.backend.ranking.application.port.out.RankingRepository;
import com.trendizm.backend.ranking.domain.entity.TrendRanking;
import com.trendizm.backend.ranking.domain.entity.TrendRankingCategory;
import com.trendizm.backend.ranking.domain.entity.TrendRankingCollectStatus;
import com.trendizm.backend.ranking.domain.entity.TrendRankingLink;
import com.trendizm.backend.ranking.domain.enums.Platform;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

/** 트렌드 순위 테이블 4개의 칼럼 매핑과 스냅샷 순위 유니크 제약을 실제 DB로 확인한다 */
@SpringBootTest
@Transactional
class RankingPersistenceTest {

    private static final LocalDateTime COLLECTED_AT = LocalDateTime.of(2026, 11, 23, 9, 5, 0, 987_654_321);

    @Autowired
    private RankingRepository rankingRepository;

    @Autowired
    private RankingTabRepository tabRepository;

    @Autowired
    private RankingLinkRepository linkRepository;

    @Autowired
    private EntityManager entityManager;

    private Long categoryId;

    @BeforeEach
    void setUp() {
        categoryId = tabRepository.save(TrendRankingCategory.create("food", "맛집/음식")).getId();
    }

    @Test
    @DisplayName("순위·관련 링크·카테고리가 저장되고, 저장할 때 쓴 수집 시각과 DB 값이 같다")
    void 순위_저장() {
        TrendRanking ranking = rankingRepository.save(TrendRanking.create(
                categoryId, Platform.youtube, 1, "두쫀쿠", COLLECTED_AT.minusDays(7), COLLECTED_AT));
        ranking.changeSummary("두바이 쫀득 쿠키 줄임말");
        linkRepository.save(TrendRankingLink.create(ranking.getId(), "영상 제목", "https://youtu.be/abc",
                null, "설명", LocalDateTime.of(2026, 11, 20, 18, 0), 0));
        LocalDateTime savedCollectedAt = ranking.getCollectedAt();
        entityManager.flush();
        entityManager.clear();

        TrendRanking found = rankingRepository.findById(ranking.getId()).orElseThrow();
        assertThat(found.getPlatform()).isEqualTo(Platform.youtube);
        assertThat(found.getRank()).isEqualTo(1);
        assertThat(found.getSummary()).isEqualTo("두바이 쫀득 쿠키 줄임말");
        assertThat(found.getCollectedAt()).isEqualTo(savedCollectedAt);

        List<TrendRankingLink> links = linkRepository.findByRankingIdInOrderBySortOrderAscIdAsc(List.of(ranking.getId()));
        assertThat(links).singleElement().satisfies(link -> {
            assertThat(link.getThumbnail()).isNull();
            assertThat(link.getSortOrder()).isZero();
        });

        assertThat(tabRepository.findAllByOrderByIdAsc()).extracting(TrendRankingCategory::getTitle)
                .containsExactly("맛집/음식");
    }

    @Test
    @DisplayName("플랫폼은 DB에 소문자 slug 그대로 저장된다")
    void 플랫폼_저장값() {
        Long rankingId = rankingRepository.save(
                TrendRanking.create(categoryId, Platform.tiktok, 1, "키워드", null, COLLECTED_AT)).getId();
        entityManager.flush();

        Object stored = entityManager.createNativeQuery("select platform from trend_rankings where id = :id")
                .setParameter("id", rankingId)
                .getSingleResult();

        assertThat(stored).isEqualTo("tiktok");
    }

    @Test
    @DisplayName("한 스냅샷(같은 플랫폼·카테고리·수집 시각) 안에서 같은 순위는 두 번 들어갈 수 없다")
    void 스냅샷_순위_중복() {
        rankingRepository.save(TrendRanking.create(categoryId, Platform.youtube, 1, "첫 키워드", null, COLLECTED_AT));
        // 다른 플랫폼, 다른 스냅샷이면 같은 순위여도 된다
        rankingRepository.save(TrendRanking.create(categoryId, Platform.x, 1, "첫 키워드", null, COLLECTED_AT));
        rankingRepository.save(TrendRanking.create(
                categoryId, Platform.youtube, 1, "첫 키워드", null, COLLECTED_AT.plusHours(12)));
        entityManager.flush();

        assertThatThrownBy(() -> {
            rankingRepository.save(TrendRanking.create(categoryId, Platform.youtube, 1, "다른 키워드", null, COLLECTED_AT));
            entityManager.flush();
        }).isInstanceOfAny(DataIntegrityViolationException.class, org.hibernate.exception.ConstraintViolationException.class);
    }

    @Test
    @DisplayName("수집 설정은 수집하지 않는 상태로 저장되고, 켜면 변경 감지로 저장된다")
    void 수집_설정_저장() {
        TrendRankingCollectStatus collectStatus = TrendRankingCollectStatus.create(Platform.youtube, 50, "viewCount", 2);
        entityManager.persist(collectStatus);
        entityManager.flush();
        entityManager.clear();

        TrendRankingCollectStatus found = entityManager.find(TrendRankingCollectStatus.class, collectStatus.getId());
        assertThat(found.getPlatform()).isEqualTo(Platform.youtube);
        assertThat(found.isStatus()).isFalse();
        assertThat(found.getCount()).isEqualTo(50);
        assertThat(found.getTimes()).isEqualTo(2);

        found.changeStatus(true);
        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(TrendRankingCollectStatus.class, collectStatus.getId()).isStatus()).isTrue();
    }
}
