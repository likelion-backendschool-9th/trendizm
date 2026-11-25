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
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
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
 *   3) 쓰기: 문서를 다시 읽어 그사이 숨겨졌으면 저장하지 않는다. Meme은 @DynamicUpdate라 요약 칼럼만 UPDATE한다
 *   메서드 안에서 저장소를 트랜잭션 템플릿 밖에서 부르지 않는다. NOT_SUPPORTED 범위에서 저장소를 부르면
 *   그 범위가 끝날 때까지 EntityManager와 연결이 붙잡혀 AI를 기다리는 동안에도 반환되지 않는다.
 * 무료 API 한도를 아끼려고 다음 경우에는 만들지 않는다.
 * - API 키가 없음, 의견이 MIN_OPINIONS개 미만, 숨김 문서
 * - 마지막 요약 후 minInterval이 안 지남. 그 사이 바뀐 내용은 그다음 변경 때 함께 반영된다
 * - 마지막 시도가 실패한 지 minInterval이 안 지남. 한도 초과(429) 때 투표마다 다시 불러 한도를 더 쓰지 않게 한다.
 *   시도 시각은 서버 메모리에만 둔다(서버가 한 대라 충분하고, 다시 켜지면 한 번 더 시도할 뿐이다)
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

    private final MemeRepository memeRepository;
    private final MemeInterpretationRepository interpretationRepository;
    private final MemeSummaryGenerator summaryGenerator;
    private final TransactionTemplate readTransaction;
    private final TransactionTemplate writeTransaction;
    private final Duration minInterval;
    private final Map<Long, LocalDateTime> failedAttempts = new ConcurrentHashMap<>();

    public MemeSummaryServiceImpl(MemeRepository memeRepository,
                                  MemeInterpretationRepository interpretationRepository,
                                  MemeSummaryGenerator summaryGenerator,
                                  TransactionTemplate transactionTemplate,
                                  @Value("${trendizm.summary.min-interval:PT10M}") Duration minInterval) {
        this.memeRepository = memeRepository;
        this.interpretationRepository = interpretationRepository;
        this.summaryGenerator = summaryGenerator;
        this.writeTransaction = transactionTemplate;
        this.readTransaction = new TransactionTemplate(transactionTemplate.getTransactionManager());
        this.readTransaction.setReadOnly(true);
        this.minInterval = minInterval;
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
        if (!summaryGenerator.isEnabled()) {
            return;
        }
        try {
            LocalDateTime now = LocalDateTime.now();
            if (failedRecently(memeId, now)) {
                return;
            }
            Optional<SummaryTarget> target = readTransaction.execute(status -> findTarget(memeId, now));
            if (target == null || target.isEmpty()) {
                return;
            }
            Optional<String> summary = summaryGenerator.summarize(target.get().word(), target.get().opinions())
                    .map(String::strip)
                    .filter(text -> !text.isEmpty())
                    .map(text -> SummaryText.fit(text, MAX_SUMMARY_LENGTH));
            if (summary.isEmpty()) {
                failedAttempts.put(memeId, now);
                return;
            }
            failedAttempts.remove(memeId);
            writeTransaction.executeWithoutResult(status -> memeRepository.findById(memeId)
                    .filter(found -> !found.isHidden()) // AI를 기다리는 사이 숨겨졌으면 저장하지 않는다
                    .ifPresent(found -> found.changeAiSummary(summary.get(), LocalDateTime.now())));
        } catch (RuntimeException e) {
            // 비동기라 호출한 쪽에 전달되지 않는다. 요약은 없어도 되므로 기록만 한다
            failedAttempts.put(memeId, LocalDateTime.now());
            log.warn("AI 요약 실패 memeId={}", memeId, e);
        }
    }

    /** 요약할 문서면 단어와 신뢰도순 상위 의견을 꺼낸다. 숨김·최근 요약·의견 부족이면 empty */
    private Optional<SummaryTarget> findTarget(Long memeId, LocalDateTime now) {
        Optional<Meme> meme = memeRepository.findById(memeId)
                .filter(found -> !found.isHidden())
                .filter(found -> !found.isAiSummaryFresh(now, minInterval));
        if (meme.isEmpty()) {
            return Optional.empty();
        }
        List<SummaryOpinion> opinions = interpretationRepository
                .findByMemeIdOrderByTrust(memeId, PageRequest.of(0, MAX_OPINIONS)).getContent().stream()
                .map(opinion -> new SummaryOpinion(opinion.getMeaning(), opinion.getContent(),
                        opinion.getAgreeCount(), opinion.getDisagreeCount()))
                .toList();
        return opinions.size() < MIN_OPINIONS ? Optional.empty() : Optional.of(new SummaryTarget(meme.get().getWord(), opinions));
    }

    /** 트랜잭션 밖으로 꺼내는 요약 재료. 엔티티 대신 값만 담는다 */
    private record SummaryTarget(String word, List<SummaryOpinion> opinions) {
    }

    private boolean failedRecently(Long memeId, LocalDateTime now) {
        LocalDateTime failedAt = failedAttempts.get(memeId);
        return failedAt != null && failedAt.isAfter(now.minus(minInterval));
    }
}
