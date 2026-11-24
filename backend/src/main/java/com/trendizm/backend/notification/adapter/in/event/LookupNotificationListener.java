package com.trendizm.backend.notification.adapter.in.event;

import com.trendizm.backend.lookup.application.dto.MemeInterpretationAddedEvent;
import com.trendizm.backend.lookup.application.dto.MemeInterpretationVotedEvent;
import com.trendizm.backend.notification.application.dto.CreateNotificationCommand;
import com.trendizm.backend.notification.application.port.NotificationService;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 밈 검색(lookup)에서 일어난 일을 알림으로 만든다 (11.4).
 * - 투표·해석 작성이 커밋된 뒤에만 받는다(@TransactionalEventListener 기본값 AFTER_COMMIT). 롤백되면 알림도 없다.
 * - 받은 이벤트는 알림 전용 스레드 1개에 넘기고 바로 돌아간다. 요청 스레드에서 알림을 저장하면, 커밋 뒤에도
 *   요청이 DB 연결을 아직 쥔 채로 연결을 하나 더 빌린다. 투표가 연결 풀 크기(10)만큼 몰리면 모두가 두 번째 연결을
 *   기다리며 멈추고, 30초 뒤 타임아웃까지 서버의 모든 DB 요청이 막힌다. 전용 스레드는 한 번에 연결 하나만 쓴다.
 * - 대기열이 차면(MAX_QUEUE) 그 알림은 버리고 로그만 남긴다. 알림 때문에 투표가 느려지거나 실패하지 않게 한다.
 * - 알림을 만들다 실패해도 로그만 남긴다. 투표·해석 작성은 이미 성공했다.
 * - lookup은 알림 도메인을 모르고 이벤트만 발행한다. 그래서 lookup → notification 의존이 생기지 않는다.
 * - 실행기를 Spring 빈(Executor)으로 등록하지 않는다. 등록하면 Spring Boot가 기본 실행기(applicationTaskExecutor)를
 *   만들지 않아 다른 비동기 기능까지 이 스레드 하나를 쓰게 된다.
 */
@Slf4j
@Component
public class LookupNotificationListener {

    static final int MAX_QUEUE = 1_000;

    private final NotificationService notificationService;
    private final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    /** 맡겼지만 아직 끝나지 않은 알림 수. 실행기의 작업 수는 대기열에서 꺼낸 직후 잠깐 0으로 보여서 따로 센다 */
    private final AtomicInteger pending = new AtomicInteger();

    public LookupNotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(MAX_QUEUE);
        executor.setThreadNamePrefix("notification-");
        executor.setRejectedExecutionHandler((task, pool) -> {
            pending.decrementAndGet();
            log.warn("알림 대기열이 가득 차서 알림 하나를 버림");
        });
        // 서버를 끌 때 대기 중인 알림을 최대 5초까지 마저 저장한다(Docker는 10초 뒤 강제 종료)
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(5);
        executor.initialize();
    }

    @TransactionalEventListener
    public void onVoted(MemeInterpretationVotedEvent event) {
        notifyLater(CreateNotificationCommand.memeVote(event.authorId(), event.voterId(), event.memeId(),
                event.interpretationId(), event.word(), event.agreeCount()));
    }

    @TransactionalEventListener
    public void onInterpretationAdded(MemeInterpretationAddedEvent event) {
        notifyLater(CreateNotificationCommand.memeInterpretation(event.memeOwnerId(), event.authorId(),
                event.memeId(), event.interpretationId(), event.word()));
    }

    /** 처리 중이거나 기다리는 알림이 없는지. 테스트가 알림 저장이 끝나기를 기다릴 때 쓴다 */
    public boolean isIdle() {
        return pending.get() == 0;
    }

    @PreDestroy
    void shutdown() {
        executor.shutdown();
    }

    private void notifyLater(CreateNotificationCommand command) {
        pending.incrementAndGet();
        executor.execute(() -> {
            try {
                notificationService.notify(command);
            } catch (RuntimeException e) {
                log.warn("알림 저장 실패 type={} receiverId={}", command.type(), command.receiverId(), e);
            } finally {
                pending.decrementAndGet();
            }
        });
    }
}
