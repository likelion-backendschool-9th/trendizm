package com.trendizm.backend.ranking.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.ranking.application.dto.RankingSummaryResult;
import com.trendizm.backend.ranking.application.dto.RankingSummaryResult.SkippedKeyword;
import com.trendizm.backend.ranking.application.dto.RankingSummaryResult.SummarizedKeyword;
import com.trendizm.backend.ranking.application.exception.RankingErrorCode;
import com.trendizm.backend.ranking.application.port.RankingSummaryService;
import com.trendizm.backend.ranking.application.port.out.RankingLinkRepository;
import com.trendizm.backend.ranking.application.port.out.RankingRepository;
import com.trendizm.backend.ranking.application.port.out.RankingSummaryGenerator;
import com.trendizm.backend.ranking.application.port.out.RankingSummaryGenerator.Answer;
import com.trendizm.backend.ranking.application.port.out.RankingSummaryGenerator.Post;
import com.trendizm.backend.ranking.application.port.out.RankingSummaryGenerator.Target;
import com.trendizm.backend.ranking.application.port.out.RankingTabRepository;
import com.trendizm.backend.ranking.domain.entity.TrendRanking;
import com.trendizm.backend.ranking.domain.entity.TrendRankingCategory;
import com.trendizm.backend.ranking.domain.entity.TrendRankingLink;
import com.trendizm.backend.ranking.domain.enums.Platform;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 순위 AI 요약. Gemini 호출(RankingSummaryGenerator)은 mock으로 바꾼다.
 * 요약은 트랜잭션 밖에서 AI를 부르고 따로 저장해서 테스트 데이터도 커밋되어야 한다.
 * 그래서 @Transactional을 붙이지 않고, 테스트가 끝나면 테이블을 비운다.
 */
@SpringBootTest
@Sql(statements = {"DELETE FROM trend_ranking_links", "DELETE FROM trend_rankings", "DELETE FROM trend_ranking_categories"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class RankingSummaryServiceTest {

    private static final LocalDateTime COLLECTED_AT = LocalDateTime.of(2026, 11, 23, 12, 16);

    @Autowired
    private RankingSummaryService summaryService;

    @Autowired
    private RankingRepository rankingRepository;

    @Autowired
    private RankingLinkRepository linkRepository;

    @Autowired
    private RankingTabRepository tabRepository;

    @MockitoBean
    private RankingSummaryGenerator summaryGenerator;

    private Long challengeId;

    @BeforeEach
    void setUp() {
        challengeId = tabRepository.save(TrendRankingCategory.create("challenge", "챌린지")).getId();
        tabRepository.save(TrendRankingCategory.create("uncategorized", "미분류"));
    }

    private Long saveRanking(int rank, String keyword, LocalDateTime collectedAt) {
        return rankingRepository.save(TrendRanking.create(challengeId, Platform.instagram, rank, keyword, null, collectedAt))
                .getId();
    }

    private void saveLink(Long rankingId, int sortOrder, String title, String description) {
        linkRepository.save(TrendRankingLink.create(rankingId, title, "https://www.instagram.com/p/a" + sortOrder + "/",
                null, description, null, sortOrder));
    }

    private String summaryOf(Long rankingId) {
        return rankingRepository.findById(rankingId).orElseThrow().getSummary();
    }

    @Test
    @DisplayName("최근 스냅샷 키워드의 링크 제목·설명을 순서대로 보내 요약하고 trend_rankings.summary에 저장한다")
    void 요약_저장() {
        Long old = saveRanking(1, "지난 키워드", COLLECTED_AT.minusDays(7));
        saveLink(old, 0, "지난 글", null);
        Long id = saveRanking(1, "걸그립", COLLECTED_AT);
        saveLink(id, 1, "검색 제목", "손에 소지품을 가득 쥐고 보여준다");
        saveLink(id, 0, "소지품을 한 손에 쥐는 챌린지", null);
        saveLink(id, 2, null, null);
        given(summaryGenerator.summarize(anyList())).willAnswer(call -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
            return List.of(new Answer(id, " 소지품을 한 손에 쥐고\n보여주는 챌린지다. ", "소지품을 한 손에  쥐는 챌린지"));
        });

        RankingSummaryResult result = summaryService.summarizeLatest("instagram", "challenge", false);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Target>> captor = ArgumentCaptor.forClass(List.class);
        verify(summaryGenerator).summarize(captor.capture());
        assertThat(captor.getValue()).containsExactly(new Target(id, "걸그립", List.of(
                new Post("소지품을 한 손에 쥐는 챌린지", null),
                new Post("검색 제목", "손에 소지품을 가득 쥐고 보여준다"))));
        assertThat(result.collectedAt()).isEqualTo(COLLECTED_AT);
        assertThat(result.skipped()).isEmpty();
        assertThat(result.summarized()).extracting(SummarizedKeyword::keyword, SummarizedKeyword::summary)
                .containsExactly(tuple("걸그립", "소지품을 한 손에 쥐고 보여주는 챌린지다."));
        assertThat(summaryOf(id)).isEqualTo("소지품을 한 손에 쥐고 보여주는 챌린지다.");
        assertThat(summaryOf(old)).isNull();
    }

    @Test
    @DisplayName("확인하지 못한 요약과 글이 없는 키워드는 저장하지 않고 이유를 알린다")
    void 저장하지_않는_키워드() {
        Long unknown = saveRanking(1, "모름", COLLECTED_AT);
        Long noEvidence = saveRanking(2, "근거없음", COLLECTED_AT);
        Long noText = saveRanking(3, "글없음", COLLECTED_AT);
        Long good = saveRanking(4, "정상", COLLECTED_AT);
        for (Long id : List.of(unknown, noEvidence, good)) {
            saveLink(id, 0, "게시물 글이다", null);
        }
        saveLink(noText, 0, null, null);
        given(summaryGenerator.summarize(anyList())).willReturn(List.of(
                new Answer(good, "요약이다.", "게시물 글"),
                new Answer(unknown, "", ""),
                new Answer(noEvidence, "지어낸 요약이다.", "글에 없는 구절")));

        RankingSummaryResult result = summaryService.summarizeLatest("instagram", "challenge", false);

        assertThat(result.summarized()).extracting(SummarizedKeyword::keyword).containsExactly("정상");
        assertThat(result.skipped()).extracting(SkippedKeyword::keyword, SkippedKeyword::reason).containsExactlyInAnyOrder(
                tuple("글없음", "관련 게시물에 제목과 설명이 없습니다."),
                tuple("모름", "게시물 글만으로는 무엇인지 알 수 없습니다."),
                tuple("근거없음", "요약의 근거를 게시물 글에서 찾지 못했습니다."));
        assertThat(summaryOf(good)).isEqualTo("요약이다.");
        assertThat(summaryOf(unknown)).isNull();
        assertThat(summaryOf(noEvidence)).isNull();
    }

    @Test
    @DisplayName("이미 요약이 있으면 AI를 부르지 않고, overwrite면 다시 만든다. 다시 만들지 못하면 원래 요약을 지우지 않는다")
    void 다시_만들기() {
        Long id = saveRanking(1, "걸그립", COLLECTED_AT);
        saveLink(id, 0, "소지품을 쥔다", null);
        given(summaryGenerator.summarize(anyList())).willReturn(List.of(new Answer(id, "첫 요약이다.", "소지품을 쥔다")));
        summaryService.summarizeLatest("instagram", "challenge", false);

        RankingSummaryResult again = summaryService.summarizeLatest("instagram", "challenge", false);

        assertThat(again.skipped()).extracting(SkippedKeyword::reason).containsExactly("이미 요약이 있습니다.");
        verify(summaryGenerator).summarize(anyList()); // 처음 한 번뿐이다

        given(summaryGenerator.summarize(anyList())).willReturn(List.of(new Answer(id, "", "")));
        summaryService.summarizeLatest("instagram", "challenge", true);
        assertThat(summaryOf(id)).isEqualTo("첫 요약이다.");

        given(summaryGenerator.summarize(anyList())).willReturn(List.of(new Answer(id, "새 요약이다.", "소지품을 쥔다")));
        summaryService.summarizeLatest("instagram", "challenge", true);
        assertThat(summaryOf(id)).isEqualTo("새 요약이다.");
    }

    @Test
    @DisplayName("AI 호출이 실패하면 그 오류를 알리고 아무것도 저장하지 않는다")
    void AI_실패() {
        Long id = saveRanking(1, "걸그립", COLLECTED_AT);
        saveLink(id, 0, "소지품을 쥔다", null);
        given(summaryGenerator.summarize(anyList()))
                .willThrow(new BusinessException(RankingErrorCode.RANKING_SUMMARY_RATE_LIMITED));

        assertThatThrownBy(() -> summaryService.summarizeLatest("instagram", "challenge", false))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(RankingErrorCode.RANKING_SUMMARY_RATE_LIMITED));
        assertThat(summaryOf(id)).isNull();
    }

    @Test
    @DisplayName("스냅샷이 없으면 빈 결과, 없는 플랫폼·미분류 카테고리면 404이고 AI를 부르지 않는다")
    void 대상_없음() {
        RankingSummaryResult empty = summaryService.summarizeLatest("instagram", "challenge", false);

        assertThat(empty.collectedAt()).isNull();
        assertThat(empty.summarized()).isEmpty();
        assertThatThrownBy(() -> summaryService.summarizeLatest("naver", "challenge", false))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(RankingErrorCode.RANKING_PLATFORM_NOT_FOUND));
        assertThatThrownBy(() -> summaryService.summarizeLatest("instagram", "uncategorized", false))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(RankingErrorCode.RANKING_CATEGORY_NOT_FOUND));
        verifyNoInteractions(summaryGenerator);
    }
}
