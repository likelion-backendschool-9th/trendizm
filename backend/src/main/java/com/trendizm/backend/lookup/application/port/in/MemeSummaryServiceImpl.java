package com.trendizm.backend.lookup.application.port.in;

import com.trendizm.backend.lookup.application.dto.MemeOpinionsChangedEvent;
import com.trendizm.backend.lookup.application.port.MemeSummaryService;
import com.trendizm.backend.lookup.application.port.out.MemeInterpretationRepository;
import com.trendizm.backend.lookup.application.port.out.MemeRepository;
import com.trendizm.backend.lookup.application.port.out.MemeSummaryGenerator;
import com.trendizm.backend.lookup.application.port.out.SummaryOpinion;
import com.trendizm.backend.lookup.domain.entity.Meme;
import com.trendizm.backend.lookup.domain.vo.SummaryText;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 의견 AI 요약은 외부 AI 호출(수 초)이 있어서 의견 작성·투표 요청과 분리한다(썸네일과 같은 방식).
 * - 커밋된 뒤에(AFTER_COMMIT) 다른 스레드에서(@Async) 실행한다. 작성·투표 응답이 느려지지 않고 실패해도 영향이 없다.
 * - DB 작업은 짧은 트랜잭션 두 개로 나누고, 그 사이의 AI 호출(수 초)은 트랜잭션과 DB 연결 없이 한다.
 *   1) 읽기: 문서·의견을 조회해 단어와 의견 데이터(SummaryTarget)만 꺼낸다
 *   2) AI 호출: 트랜잭션 밖(NOT_SUPPORTED)이고, 1)의 연결은 이미 반환됐다
 *   3) 쓰기: 문서를 다시 읽어 그사이 숨겨졌거나 다른 요약이 저장됐으면(요약 시각이 1)과 다름) 저장하지 않는다.
 *      Meme은 @DynamicUpdate라 요약 칼럼만 UPDATE한다
 *   메서드 안에서 저장소를 트랜잭션 템플릿 밖에서 부르지 않는다. NOT_SUPPORTED 범위에서 저장소를 부르면
 *   그 범위가 끝날 때까지 EntityManager와 연결이 붙잡혀 AI를 기다리는 동안에도 반환되지 않는다.
 * - 같은 문서는 한 번에 하나만 만든다(inFlight). 만드는 중에 또 변경이 오면 미뤄 둔다.
 * 무료 API 한도를 아끼려고 다음 경우에는 바로 만들지 않는다.
 * - API 키가 없음, 의견이 MIN_OPINIONS개 미만, 숨김 문서: 만들지 않는다(다음 변경 때 다시 본다)
 * - 마지막 요약 후 minInterval이 안 지남, 마지막 실패 후 minInterval이 안 지남, 한도 초과(429)로 잠시 멈춤:
 *   미뤄 둔다(pending). refreshPendingSummaries가 1분마다 돌며 때가 된 문서를 다시 만든다.
 *   그래서 간격 안에 들어온 변경도 그 뒤 활동이 없더라도 간격이 지나면 반영된다.
 * - 실패(시간 초과, 429, 차단된 응답 등)하면 minInterval 뒤에 다시 시도하도록 미룬다. 계속 실패하는 문서(안전 필터 등)로
 *   한도를 쓰지 않게 연속 MAX_RETRIES번까지만 저절로 다시 시도하고, 그 뒤에는 그 문서에 다음 변경이 올 때 다시 본다.
 * - 미뤄 둔 문서는 스케줄러 스레드가 아니라 앱 실행기(applicationTaskExecutor)에서 한 번에 하나씩 만든다.
 *   스케줄러 스레드는 하나뿐이고 알림 SSE 연결 유지 신호(25초마다)도 쓰므로, AI 호출(최대 20초)로 막지 않는다.
 *   한꺼번에 여러 문서를 동시에 부르지 않아 무료 한도(분당 요청 수)도 아낀다.
 * 실패 기록·미뤄 둔 문서는 서버 메모리에만 둔다(서버가 한 대라 충분하다. 재시작하면 미뤄 둔 문서는 다음 변경 때 반영된다)
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class MemeSummaryServiceImpl implements MemeSummaryService {

    /** 요약을 만드는 최소 의견 수. 의견이 하나면 그 의견을 그대로 보면 된다 */
    static final int MIN_OPINIONS = 2;
    /** 요약에 넣는 의견 수 (신뢰도순 상위) */
    static final int MAX_OPINIONS = 10;
    /** 저장하는 요약 최대 길이. AI가 지시보다 길게 써도 여기서 문장 단위로 자른다(SummaryText.fit) */
    static final int MAX_SUMMARY_LENGTH = 500;
    /** 실패한 문서를 저절로 다시 시도하는 최대 연속 횟수 */
    static final int MAX_RETRIES = 3;

    private final MemeRepository memeRepository;
    private final MemeInterpretationRepository interpretationRepository;
    private final MemeSummaryGenerator summaryGenerator;
    private final TransactionTemplate readTransaction;
    private final TransactionTemplate writeTransaction;
    private final Duration minInterval;
    private final TaskExecutor taskExecutor;
    /** 문서별 마지막 실패 시각과 연속 실패 횟수. 성공하면 지운다 */
    private final Map<Long, Failure> failures = new ConcurrentHashMap<>();
    /** 미뤄 둔 문서를 처리하는 작업이 실행기에서 돌고 있으면 true. 앞 작업이 안 끝났으면 새로 넣지 않는다 */
    private final AtomicBoolean sweeping = new AtomicBoolean();
    /** 지금 요약을 만들고 있는 문서 */
    private final Set<Long> inFlight = ConcurrentHashMap.newKeySet();
    /** 미뤄 둔 문서와 다시 만들 시각 */
    private final Map<Long, LocalDateTime> pending = new ConcurrentHashMap<>();

    public MemeSummaryServiceImpl(MemeRepository memeRepository,
                                  MemeInterpretationRepository interpretationRepository,
                                  MemeSummaryGenerator summaryGenerator,
                                  TransactionTemplate transactionTemplate,
                                  @Qualifier("applicationTaskExecutor") TaskExecutor taskExecutor,
                                  @Value("${trendizm.summary.min-interval:PT10M}") Duration minInterval) {
        this.memeRepository = memeRepository;
        this.interpretationRepository = interpretationRepository;
        this.summaryGenerator = summaryGenerator;
        this.writeTransaction = transactionTemplate;
        this.readTransaction = new TransactionTemplate(transactionTemplate.getTransactionManager());
        this.readTransaction.setReadOnly(true);
        this.minInterval = minInterval;
        this.taskExecutor = taskExecutor;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void onOpinionsChanged(MemeOpinionsChangedEvent event) {
        refreshSummary(event.memeId());
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void refreshSummary(Long memeId) {
        if (!summaryGenerator.isConfigured()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        if (!inFlight.add(memeId)) {
            // 같은 문서를 만드는 중이다. 끝난 뒤 다시 보도록 미뤄 둔다(그때 간격이 안 지났으면 다시 미룬다)
            defer(memeId, now);
            return;
        }
        try {
            refresh(memeId, now);
        } catch (RuntimeException e) {
            // 비동기라 호출한 쪽에 전달되지 않는다. 요약은 없어도 되므로 기록만 한다
            recordFailure(memeId);
            log.warn("AI 요약 실패 memeId={}", memeId, e);
        } finally {
            inFlight.remove(memeId);
        }
    }

    /**
     * 1분마다 미뤄 둔 문서 처리를 실행기에 넘긴다. 스케줄러 스레드에서는 AI를 부르지 않는다.
     * 앞 작업이 아직 돌고 있으면 넘기지 않는다(같은 문서를 두 작업이 함께 보지 않게)
     */
    @Scheduled(fixedDelayString = "${trendizm.summary.pending-check:PT1M}")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void schedulePendingSummaries() {
        if (pending.isEmpty() || !sweeping.compareAndSet(false, true)) {
            return;
        }
        try {
            taskExecutor.execute(() -> {
                try {
                    refreshPendingSummaries();
                } finally {
                    sweeping.set(false);
                }
            });
        } catch (RuntimeException e) {
            sweeping.set(false); // 실행기가 거절하면 다음 주기에 다시 넘긴다
            log.warn("미뤄 둔 AI 요약 작업을 넘기지 못했습니다", e);
        }
    }

    /** 미뤄 둔 문서 중 때가 된 것을 부른 스레드에서 하나씩 다시 만든다 */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void refreshPendingSummaries() {
        LocalDateTime now = LocalDateTime.now();
        pending.forEach((memeId, dueAt) -> {
            if (!dueAt.isAfter(now) && pending.remove(memeId, dueAt)) {
                refreshSummary(memeId);
            }
        });
    }

    private void refresh(Long memeId, LocalDateTime now) {
        Failure failure = failures.get(memeId);
        if (failure != null && failure.at().isAfter(now.minus(minInterval))) {
            defer(memeId, failure.at().plus(minInterval)); // 실패 직후 들어온 변경은 간격이 지난 뒤 반영한다
            return;
        }
        if (!summaryGenerator.isEnabled()) {
            defer(memeId, now.plus(minInterval)); // 한도 초과(429)로 잠시 멈춘 상태
            return;
        }
        Check check = readTransaction.execute(status -> check(memeId, now));
        if (check == null || check.target() == null) {
            if (check != null && check.deferUntil() != null) {
                defer(memeId, check.deferUntil());
            }
            return;
        }
        SummaryTarget target = check.target();
        Optional<String> summary = summaryGenerator.summarize(target.word(), target.opinions())
                .map(String::strip)
                .filter(text -> !text.isEmpty())
                .map(text -> SummaryText.fit(text, MAX_SUMMARY_LENGTH));
        if (summary.isEmpty()) {
            recordFailure(memeId);
            return;
        }
        failures.remove(memeId);
        Boolean saved = writeTransaction.execute(status -> memeRepository.findById(memeId)
                .filter(found -> !found.isHidden()) // AI를 기다리는 사이 숨겨졌으면 저장하지 않는다
                // 그사이 다른 요약이 저장됐으면 그 요약이 더 새 의견으로 만든 것일 수 있어 덮어쓰지 않는다
                .filter(found -> Objects.equals(found.getAiSummaryUpdatedAt(), target.summaryUpdatedAt()))
                .map(found -> {
                    found.changeAiSummary(summary.get(), LocalDateTime.now());
                    return true;
                })
                .orElse(false));
        if (!Boolean.TRUE.equals(saved)) {
            defer(memeId, LocalDateTime.now()); // 다음 확인 때 최신 상태로 다시 본다(간격이 안 지났으면 다시 미룬다)
        }
    }

    /** 요약할 문서면 단어·신뢰도순 상위 의견·지금 요약 시각을 꺼낸다. 최근에 만들었으면 다시 만들 시각을 준다 */
    private Check check(Long memeId, LocalDateTime now) {
        Optional<Meme> meme = memeRepository.findById(memeId).filter(found -> !found.isHidden());
        if (meme.isEmpty()) {
            return Check.SKIP;
        }
        if (meme.get().isAiSummaryFresh(now, minInterval)) {
            return new Check(null, meme.get().getAiSummaryUpdatedAt().plus(minInterval));
        }
        List<SummaryOpinion> opinions = interpretationRepository
                .findByMemeIdOrderByTrust(memeId, PageRequest.of(0, MAX_OPINIONS)).getContent().stream()
                .map(opinion -> new SummaryOpinion(opinion.getMeaning(), opinion.getContent(),
                        opinion.getAgreeCount(), opinion.getDisagreeCount()))
                .toList();
        if (opinions.size() < MIN_OPINIONS) {
            return Check.SKIP;
        }
        return new Check(new SummaryTarget(meme.get().getWord(), opinions, meme.get().getAiSummaryUpdatedAt()), null);
    }

    /** 다시 만들 시각을 기록한다. 이미 미뤄 둔 문서면 더 이른 시각을 남긴다 */
    private void defer(Long memeId, LocalDateTime dueAt) {
        pending.merge(memeId, dueAt, (before, after) -> before.isBefore(after) ? before : after);
    }

    /** 트랜잭션 밖으로 꺼내는 요약 재료. 엔티티 대신 값만 담는다. summaryUpdatedAt은 저장할 때 그사이 다른 요약이 저장됐는지 비교한다 */
    private record SummaryTarget(String word, List<SummaryOpinion> opinions, LocalDateTime summaryUpdatedAt) {
    }

    /** target이 있으면 지금 만들고, deferUntil이 있으면 그때까지 미루고, 둘 다 없으면 만들지 않는다 */
    private record Check(SummaryTarget target, LocalDateTime deferUntil) {
        static final Check SKIP = new Check(null, null);
    }

    /** 실패를 기록하고, 연속 실패가 MAX_RETRIES번 미만이면 minInterval 뒤에 다시 시도하도록 미룬다 */
    private void recordFailure(Long memeId) {
        LocalDateTime now = LocalDateTime.now();
        Failure failure = failures.merge(memeId, new Failure(now, 1),
                (before, ignored) -> new Failure(now, before.count() + 1));
        if (failure.count() < MAX_RETRIES) {
            defer(memeId, now.plus(minInterval));
        }
    }

    private record Failure(LocalDateTime at, int count) {
    }
}
