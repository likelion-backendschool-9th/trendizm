package com.trendizm.backend.lookup.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.trendizm.backend.lookup.application.dto.AddMemeInterpretationCommand;
import com.trendizm.backend.lookup.application.port.MemeInterpretationService;
import com.trendizm.backend.lookup.application.port.MemeSummaryService;
import com.trendizm.backend.lookup.application.port.out.MemeInterpretationRepository;
import com.trendizm.backend.lookup.application.port.out.MemeRepository;
import com.trendizm.backend.lookup.application.port.out.MemeSummaryGenerator;
import com.trendizm.backend.lookup.application.port.out.SummaryOpinion;
import com.trendizm.backend.lookup.domain.entity.Meme;
import com.trendizm.backend.lookup.domain.entity.MemeInterpretation;
import com.trendizm.backend.member.application.dto.RegisterMemberCommand;
import com.trendizm.backend.member.application.port.MemberService;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 의견 AI 요약. AI 호출(MemeSummaryGenerator)은 mock으로 바꾼다.
 * 요약은 커밋 뒤 다른 스레드·트랜잭션에서 만들어서 테스트 데이터도 커밋되어야 한다.
 * 그래서 @Transactional을 붙이지 않고, 테스트가 끝나면 테이블을 비운다.
 */
@SpringBootTest
@Sql(statements = {"DELETE FROM meme_interpretation_votes", "DELETE FROM meme_interpretations", "DELETE FROM memes",
        "DELETE FROM users"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class MemeSummaryServiceTest {

    @Autowired
    private MemeSummaryService summaryService;

    @Autowired
    private MemeInterpretationService interpretationService;

    @Autowired
    private MemeRepository memeRepository;

    @Autowired
    private MemeInterpretationRepository interpretationRepository;

    @Autowired
    private MemberService memberService;

    @MockitoBean
    private MemeSummaryGenerator summaryGenerator;

    private Long memeId;

    @BeforeEach
    void setUp() {
        given(summaryGenerator.isEnabled()).willReturn(true);
        memeId = memeRepository.save(Meme.create("TMI", 1L, "https://youtu.be/tmi01", null)).getId();
    }

    private void saveOpinion(String meaning, int agree, int disagree) {
        MemeInterpretation interpretation = MemeInterpretation.create(memeId, 1L, meaning, meaning + " 내용");
        ReflectionTestUtils.setField(interpretation, "agreeCount", agree);
        ReflectionTestUtils.setField(interpretation, "disagreeCount", disagree);
        interpretationRepository.save(interpretation);
    }

    private Meme meme() {
        return memeRepository.findById(memeId).orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private List<SummaryOpinion> sentOpinions() {
        ArgumentCaptor<List<SummaryOpinion>> captor = ArgumentCaptor.forClass(List.class);
        verify(summaryGenerator).summarize(eq("tmi"), captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("의견이 2개 이상이면 신뢰도(동의 - 비동의)순 상위 10개를 보내고, 받은 요약과 만든 시각을 저장한다")
    void 요약_저장() {
        for (int i = 0; i < 12; i++) {
            saveOpinion("뜻" + i, i, 0);
        }
        saveOpinion("반대 많은 뜻", 20, 30);
        given(summaryGenerator.summarize(eq("tmi"), anyList())).willReturn(Optional.of(" 요약 결과 "));

        summaryService.refreshSummary(memeId);

        List<SummaryOpinion> sent = sentOpinions();
        assertThat(sent).hasSize(10);
        assertThat(sent.getFirst()).isEqualTo(new SummaryOpinion("뜻11", "뜻11 내용", 11, 0));
        assertThat(sent).extracting(SummaryOpinion::meaning).doesNotContain("반대 많은 뜻", "뜻0", "뜻1");
        assertThat(meme().getAiSummary()).isEqualTo("요약 결과");
        assertThat(meme().getAiSummaryUpdatedAt()).isAfter(LocalDateTime.now().minusMinutes(1));
    }

    @Test
    @DisplayName("의견이 1개 이하면 AI를 부르지 않는다")
    void 의견_부족() {
        summaryService.refreshSummary(memeId);
        saveOpinion("하나뿐인 뜻", 1, 0);
        summaryService.refreshSummary(memeId);

        verify(summaryGenerator, never()).summarize(any(), any());
        assertThat(meme().getAiSummary()).isNull();
    }

    @Test
    @DisplayName("요약을 만든 지 10분이 안 지났으면 다시 만들지 않는다")
    void 간격_제한() {
        saveOpinion("뜻1", 1, 0);
        saveOpinion("뜻2", 0, 0);
        given(summaryGenerator.summarize(eq("tmi"), anyList())).willReturn(Optional.of("첫 요약"));

        summaryService.refreshSummary(memeId);
        summaryService.refreshSummary(memeId);

        verify(summaryGenerator, times(1)).summarize(any(), any());
        assertThat(meme().getAiSummary()).isEqualTo("첫 요약");
    }

    @Test
    @DisplayName("요약이 실패(429 등으로 empty)하면 저장하지 않고, 10분 안에는 다시 시도하지 않는다 (무료 한도를 더 쓰지 않게)")
    void 실패_후_재시도_안_함() {
        saveOpinion("뜻1", 1, 0);
        saveOpinion("뜻2", 0, 0);
        given(summaryGenerator.summarize(eq("tmi"), anyList())).willReturn(Optional.empty());

        summaryService.refreshSummary(memeId);
        summaryService.refreshSummary(memeId);

        verify(summaryGenerator, times(1)).summarize(any(), any());
        assertThat(meme().getAiSummary()).isNull();
        assertThat(meme().getAiSummaryUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("AI 호출 중 예외가 나도 밖으로 던지지 않는다(비동기 실행)")
    void 예외() {
        saveOpinion("뜻1", 1, 0);
        saveOpinion("뜻2", 0, 0);
        given(summaryGenerator.summarize(eq("tmi"), anyList())).willThrow(new IllegalStateException("연결 실패"));

        summaryService.refreshSummary(memeId);

        assertThat(meme().getAiSummary()).isNull();
    }

    @Test
    @DisplayName("API 키가 없거나(꺼짐), 숨김 문서, 없는 문서면 AI를 부르지 않는다")
    void 건너뛰기() {
        saveOpinion("뜻1", 1, 0);
        saveOpinion("뜻2", 0, 0);
        given(summaryGenerator.isEnabled()).willReturn(false);
        summaryService.refreshSummary(memeId);

        given(summaryGenerator.isEnabled()).willReturn(true);
        Meme hidden = meme();
        hidden.hide();
        memeRepository.save(hidden);
        summaryService.refreshSummary(memeId);
        summaryService.refreshSummary(-1L);

        verify(summaryGenerator, never()).summarize(any(), any());
    }

    @Test
    @DisplayName("AI가 지시보다 길게 써도 500자 안에서 마지막 완성 문장까지만 저장하고, 완성 문장이 없으면 말줄임표를 붙인다")
    void 길이_제한() {
        saveOpinion("뜻1", 1, 0);
        saveOpinion("뜻2", 0, 0);
        given(summaryGenerator.summarize(eq("tmi"), anyList())).willReturn(Optional.of("가나다라마바사 문장이다. ".repeat(60)));

        summaryService.refreshSummary(memeId);

        assertThat(meme().getAiSummary()).hasSizeLessThanOrEqualTo(500).endsWith("문장이다.");
    }

    @Test
    @DisplayName("완성 문장 없이 500자를 넘으면 499자에 말줄임표를 붙여 500자로 저장한다")
    void 길이_제한_문장_없음() {
        saveOpinion("뜻1", 1, 0);
        saveOpinion("뜻2", 0, 0);
        given(summaryGenerator.summarize(eq("tmi"), anyList())).willReturn(Optional.of("가".repeat(800)));

        summaryService.refreshSummary(memeId);

        assertThat(meme().getAiSummary()).hasSize(500).isEqualTo("가".repeat(499) + "…");
    }

    @Test
    @DisplayName("AI를 기다리는 동안에는 트랜잭션도 EntityManager도 붙잡고 있지 않는다 (DB 연결을 반환한 상태)")
    void AI_호출_중_연결_반환() {
        saveOpinion("뜻1", 1, 0);
        saveOpinion("뜻2", 0, 0);
        AtomicBoolean transactionActive = new AtomicBoolean(true);
        AtomicReference<Map<Object, Object>> resources = new AtomicReference<>();
        given(summaryGenerator.summarize(eq("tmi"), anyList())).willAnswer(invocation -> {
            transactionActive.set(TransactionSynchronizationManager.isActualTransactionActive());
            resources.set(TransactionSynchronizationManager.getResourceMap());
            return Optional.of("요약");
        });

        summaryService.refreshSummary(memeId);

        assertThat(transactionActive).isFalse();
        assertThat(resources.get()).isEmpty();
        assertThat(meme().getAiSummary()).isEqualTo("요약");
    }

    @Test
    @DisplayName("AI를 기다리는 사이 문서가 숨겨지면 요약을 저장하지 않고 숨김 상태도 그대로 둔다")
    void AI_호출_중_숨김() {
        saveOpinion("뜻1", 1, 0);
        saveOpinion("뜻2", 0, 0);
        given(summaryGenerator.summarize(eq("tmi"), anyList())).willAnswer(invocation -> {
            Meme hidden = meme();
            hidden.hide();
            memeRepository.save(hidden);
            return Optional.of("숨긴 뒤 도착한 요약");
        });

        summaryService.refreshSummary(memeId);

        assertThat(meme().getAiSummary()).isNull();
        assertThat(meme().isHidden()).isTrue();
    }

    @Test
    @DisplayName("의견을 써서 2개가 되면, 커밋 뒤 따로 요약을 만든다")
    void 의견_작성_후_자동() throws Exception {
        Long writer = memberService.register(new RegisterMemberCommand("summary@test.com", "encoded", "요약러", false));
        saveOpinion("뜻1", 1, 0);
        given(summaryGenerator.summarize(eq("tmi"), anyList())).willReturn(Optional.of("자동 요약"));

        interpretationService.addInterpretation(new AddMemeInterpretationCommand(memeId, writer, "뜻2", "내용"));

        Instant deadline = Instant.now().plus(Duration.ofSeconds(10));
        while (meme().getAiSummary() == null && Instant.now().isBefore(deadline)) {
            Thread.sleep(50);
        }
        assertThat(meme().getAiSummary()).isEqualTo("자동 요약");
    }
}
