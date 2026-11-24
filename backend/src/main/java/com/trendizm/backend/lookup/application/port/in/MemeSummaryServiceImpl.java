package com.trendizm.backend.lookup.application.port.in;

import com.trendizm.backend.lookup.application.dto.MemeOpinionsChangedEvent;
import com.trendizm.backend.lookup.application.port.MemeSummaryService;
import com.trendizm.backend.lookup.application.port.out.MemeInterpretationRepository;
import com.trendizm.backend.lookup.application.port.out.MemeRepository;
import com.trendizm.backend.lookup.application.port.out.MemeSummaryGenerator;
import com.trendizm.backend.lookup.application.port.out.SummaryOpinion;
import com.trendizm.backend.lookup.domain.entity.Meme;
import com.trendizm.backend.lookup.domain.entity.MemeInterpretation;
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
 * - AI 호출은 트랜잭션 밖에서 하고(NOT_SUPPORTED), 결과 기록만 짧은 트랜잭션으로 한다.
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
    /** 저장하는 요약 최대 길이. AI가 지시보다 길게 써도 여기서 자른다 */
    static final int MAX_SUMMARY_LENGTH = 500;

    private final MemeRepository memeRepository;
    private final MemeInterpretationRepository interpretationRepository;
    private final MemeSummaryGenerator summaryGenerator;
    private final TransactionTemplate transactionTemplate;
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
        this.transactionTemplate = transactionTemplate;
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
            Optional<Meme> meme = memeRepository.findById(memeId)
                    .filter(found -> !found.isHidden())
                    .filter(found -> !found.isAiSummaryFresh(now, minInterval));
            if (meme.isEmpty() || failedRecently(memeId, now)) {
                return;
            }
            List<MemeInterpretation> top = interpretationRepository
                    .findByMemeIdOrderByTrust(memeId, PageRequest.of(0, MAX_OPINIONS)).getContent();
            if (top.size() < MIN_OPINIONS) {
                return;
            }
            Optional<String> summary = summaryGenerator.summarize(meme.get().getWord(), top.stream()
                            .map(opinion -> new SummaryOpinion(opinion.getMeaning(), opinion.getContent(),
                                    opinion.getAgreeCount(), opinion.getDisagreeCount()))
                            .toList())
                    .map(String::strip)
                    .filter(text -> !text.isEmpty())
                    .map(MemeSummaryServiceImpl::truncate);
            if (summary.isEmpty()) {
                failedAttempts.put(memeId, now);
                return;
            }
            failedAttempts.remove(memeId);
            transactionTemplate.executeWithoutResult(status -> memeRepository.findById(memeId)
                    .ifPresent(found -> found.changeAiSummary(summary.get(), LocalDateTime.now())));
        } catch (RuntimeException e) {
            // 비동기라 호출한 쪽에 전달되지 않는다. 요약은 없어도 되므로 기록만 한다
            failedAttempts.put(memeId, LocalDateTime.now());
            log.warn("AI 요약 실패 memeId={}", memeId, e);
        }
    }

    private boolean failedRecently(Long memeId, LocalDateTime now) {
        LocalDateTime failedAt = failedAttempts.get(memeId);
        return failedAt != null && failedAt.isAfter(now.minus(minInterval));
    }

    private static String truncate(String summary) {
        return summary.length() > MAX_SUMMARY_LENGTH ? summary.substring(0, MAX_SUMMARY_LENGTH) : summary;
    }
}
