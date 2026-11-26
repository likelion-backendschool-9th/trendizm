package com.trendizm.backend.lookup.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.trendizm.backend.lookup.application.port.MemeSummaryService;
import com.trendizm.backend.lookup.application.port.in.MemeSummaryServiceImpl;
import com.trendizm.backend.lookup.application.port.out.MemeInterpretationRepository;
import com.trendizm.backend.lookup.application.port.out.MemeRepository;
import com.trendizm.backend.lookup.application.port.out.MemeSummaryGenerator;
import com.trendizm.backend.lookup.application.port.out.SummaryOpinion;
import com.trendizm.backend.lookup.domain.entity.Meme;
import com.trendizm.backend.lookup.domain.entity.MemeInterpretation;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

/**
 * 간격 안에 들어온 변경을 미뤘다가 반영하는지, 같은 문서를 동시에 만들지 않고 더 새 요약을 덮어쓰지 않는지 확인한다.
 * 기다리는 시간을 줄이려고 간격을 1초로 둔다. 스케줄러(1분)를 기다리지 않고 refreshPendingSummaries를 직접 부른다.
 */
@SpringBootTest(properties = {"trendizm.summary.min-interval=PT1S",
        "trendizm.summary.pending-check=PT1H"}) // 스케줄러가 테스트 중간에 끼어들지 않게 한다
@Sql(statements = {"DELETE FROM meme_interpretations", "DELETE FROM memes"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class MemeSummaryDeferTest {

    @Autowired
    private MemeSummaryService summaryService;

    /** 스케줄러가 부르는 메서드(schedulePendingSummaries)는 in 포트에 없어서 구현체로 부른다 */
    @Autowired
    private MemeSummaryServiceImpl summaryServiceImpl;

    @Autowired
    private MemeRepository memeRepository;

    @Autowired
    private MemeInterpretationRepository interpretationRepository;

    @MockitoBean
    private MemeSummaryGenerator summaryGenerator;

    private Long memeId;

    @BeforeEach
    void setUp() {
        given(summaryGenerator.isConfigured()).willReturn(true);
        given(summaryGenerator.isEnabled()).willReturn(true);
        memeId = memeRepository.save(Meme.create("TMI", 1L, "https://youtu.be/tmi01", null)).getId();
        saveOpinion("뜻1");
        saveOpinion("뜻2");
    }

    private void saveOpinion(String meaning) {
        interpretationRepository.save(MemeInterpretation.create(memeId, 1L, meaning, meaning + " 내용"));
    }

    private Meme meme() {
        return memeRepository.findById(memeId).orElseThrow();
    }

    private static void waitInterval() throws InterruptedException {
        Thread.sleep(1_100);
    }

    @SuppressWarnings("unchecked")
    private List<List<SummaryOpinion>> sentOpinions(int calls) {
        ArgumentCaptor<List<SummaryOpinion>> captor = ArgumentCaptor.forClass(List.class);
        verify(summaryGenerator, times(calls)).summarize(eq("tmi"), captor.capture());
        return captor.getAllValues();
    }

    @Test
    @DisplayName("요약 직후 간격 안에 의견이 늘면 미뤄 두었다가, 다른 활동이 없어도 간격이 지나면 새 의견까지 넣어 다시 만든다")
    void 간격_안의_변경은_미뤘다가_반영() throws Exception {
        given(summaryGenerator.summarize(eq("tmi"), anyList())).willReturn(Optional.of("의견 2개 요약"), Optional.of("의견 3개 요약"));
        summaryService.refreshSummary(memeId);

        saveOpinion("뜻3");
        summaryService.refreshSummary(memeId); // 간격 안이라 미룬다
        summaryService.refreshPendingSummaries(); // 아직 때가 아니다
        assertThat(sentOpinions(1)).hasSize(1);

        waitInterval();
        summaryService.refreshPendingSummaries();

        List<List<SummaryOpinion>> sent = sentOpinions(2);
        assertThat(sent.get(1)).extracting(SummaryOpinion::meaning).contains("뜻3");
        assertThat(meme().getAiSummary()).isEqualTo("의견 3개 요약");

        waitInterval();
        summaryService.refreshPendingSummaries(); // 미뤄 둔 것을 한 번만 처리한다
        verify(summaryGenerator, times(2)).summarize(any(), any());
    }

    @Test
    @DisplayName("같은 문서를 만드는 중에 또 변경이 오면 AI를 다시 부르지 않고 미뤄 둔다")
    void 동시에_두_번_부르지_않는다() throws Exception {
        CountDownLatch called = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        given(summaryGenerator.summarize(eq("tmi"), anyList())).willAnswer(invocation -> {
            called.countDown();
            release.await(5, TimeUnit.SECONDS);
            return Optional.of("첫 요약");
        }).willReturn(Optional.of("다음 요약"));

        CompletableFuture<Void> first = CompletableFuture.runAsync(() -> summaryService.refreshSummary(memeId));
        assertThat(called.await(5, TimeUnit.SECONDS)).isTrue();
        summaryService.refreshSummary(memeId);
        verify(summaryGenerator, times(1)).summarize(any(), any());

        release.countDown();
        first.get(5, TimeUnit.SECONDS);
        assertThat(meme().getAiSummary()).isEqualTo("첫 요약");

        waitInterval();
        summaryService.refreshPendingSummaries();
        verify(summaryGenerator, times(2)).summarize(any(), any());
        assertThat(meme().getAiSummary()).isEqualTo("다음 요약");
    }

    @Test
    @DisplayName("AI를 기다리는 사이 다른 요약이 저장됐으면 덮어쓰지 않고, 간격이 지나면 최신 의견으로 다시 만든다")
    void 더_새_요약을_덮어쓰지_않는다() throws Exception {
        given(summaryGenerator.summarize(eq("tmi"), anyList())).willAnswer(invocation -> {
            Meme other = meme();
            other.changeAiSummary("다른 쪽이 저장한 요약", LocalDateTime.now());
            memeRepository.save(other);
            return Optional.of("늦게 도착한 옛 요약");
        }).willReturn(Optional.of("다시 만든 요약"));

        summaryService.refreshSummary(memeId);
        assertThat(meme().getAiSummary()).isEqualTo("다른 쪽이 저장한 요약");

        waitInterval();
        summaryService.refreshPendingSummaries();
        assertThat(meme().getAiSummary()).isEqualTo("다시 만든 요약");
    }

    @Test
    @DisplayName("요약이 실패하면 간격을 두고 다시 시도하되 연속 3번까지만 하고, 그 뒤에는 다음 변경이 올 때 다시 시도한다")
    void 실패는_3번까지_다시_시도() throws Exception {
        given(summaryGenerator.summarize(eq("tmi"), anyList()))
                .willReturn(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of("드디어 성공한 요약"));

        summaryService.refreshSummary(memeId); // 1번째 실패
        for (int i = 0; i < 3; i++) {
            waitInterval();
            summaryService.refreshPendingSummaries(); // 2·3번째 실패, 그 뒤에는 미뤄 둔 것이 없다
        }
        verify(summaryGenerator, times(3)).summarize(any(), any());
        assertThat(meme().getAiSummary()).isNull();

        saveOpinion("뜻3");
        summaryService.refreshSummary(memeId); // 새 변경이 오면 다시 시도한다
        verify(summaryGenerator, times(4)).summarize(any(), any());
        assertThat(meme().getAiSummary()).isEqualTo("드디어 성공한 요약");
    }

    @Test
    @DisplayName("스케줄러는 미뤄 둔 문서를 실행기에 넘기고 바로 돌아온다(알림 연결 유지 신호를 막지 않는다). 앞 작업이 돌고 있으면 또 넘기지 않는다")
    void 스케줄러_스레드에서_AI를_부르지_않는다() throws Exception {
        given(summaryGenerator.summarize(eq("tmi"), anyList())).willReturn(Optional.of("첫 요약"));
        summaryService.refreshSummary(memeId);
        saveOpinion("뜻3");
        summaryService.refreshSummary(memeId); // 미뤄 둔다
        waitInterval();

        CountDownLatch called = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicReference<Thread> aiThread = new AtomicReference<>();
        given(summaryGenerator.summarize(eq("tmi"), anyList())).willAnswer(invocation -> {
            aiThread.set(Thread.currentThread());
            called.countDown();
            release.await(5, TimeUnit.SECONDS);
            return Optional.of("실행기에서 만든 요약");
        });

        long start = System.nanoTime();
        summaryServiceImpl.schedulePendingSummaries();
        assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(Duration.ofMillis(500));
        assertThat(called.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(aiThread.get()).isNotSameAs(Thread.currentThread());

        summaryServiceImpl.schedulePendingSummaries(); // 앞 작업이 돌고 있어 넘기지 않는다
        release.countDown();
        long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        while (!"실행기에서 만든 요약".equals(meme().getAiSummary()) && System.nanoTime() < deadline) {
            Thread.sleep(50);
        }
        assertThat(meme().getAiSummary()).isEqualTo("실행기에서 만든 요약");
        verify(summaryGenerator, times(2)).summarize(any(), any());
    }
}
