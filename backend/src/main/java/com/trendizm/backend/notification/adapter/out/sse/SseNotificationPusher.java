package com.trendizm.backend.notification.adapter.out.sse;

import com.trendizm.backend.notification.application.dto.UnreadCountResult;
import com.trendizm.backend.notification.application.port.out.NotificationPusher;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 회원별 SSE 연결을 서버 메모리에 들고 있다가 안 읽은 알림 수를 보낸다 (11.7).
 * - 서버가 한 대라 메모리로 충분하다. 여러 대로 늘리면 다른 서버에 연결된 화면에는 보내지 못하므로 Redis Pub/Sub 등이 필요하다.
 * - 배포로 서버가 다시 켜지면 연결이 모두 끊기고, 브라우저 EventSource가 알아서 다시 연결한다.
 */
@Slf4j
@Component
public class SseNotificationPusher implements NotificationPusher {

    /** 연결 하나를 최대한 유지하는 시간. 지나면 끊고 브라우저가 다시 연결한다(그때 로그인 상태를 다시 확인한다) */
    static final Duration TIMEOUT = Duration.ofMinutes(30);

    /** 한 회원이 동시에 열 수 있는 연결 수(탭 수). 넘으면 가장 오래된 연결을 닫는다 */
    static final int MAX_CONNECTIONS_PER_MEMBER = 5;

    static final String UNREAD_COUNT_EVENT = "unread-count";

    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    /**
     * 연결을 만들고 첫 이벤트로 지금의 안 읽은 수를 바로 보낸다.
     * 첫 이벤트를 보내야 응답 헤더가 나가서, 중간의 Next(BFF)·nginx가 응답을 기다리며 멈추지 않는다.
     */
    @Override
    public SseEmitter subscribe(Long memberId, long unreadCount) {
        SseEmitter emitter = new SseEmitter(TIMEOUT.toMillis());
        emitter.onCompletion(() -> remove(memberId, emitter));
        emitter.onTimeout(emitter::complete);
        emitter.onError(error -> remove(memberId, emitter));
        List<SseEmitter> evicted = new ArrayList<>();
        // 추가와 정리를 회원별로 한 번에 처리해서, 마지막 연결이 정리되며 목록이 지워지는 순간과 겹쳐도 새 연결을 잃지 않는다
        emitters.compute(memberId, (id, list) -> {
            List<SseEmitter> connections = list == null ? new CopyOnWriteArrayList<>() : list;
            connections.add(emitter);
            while (connections.size() > MAX_CONNECTIONS_PER_MEMBER) {
                evicted.add(connections.removeFirst());
            }
            return connections;
        });
        evicted.forEach(SseEmitter::complete);
        send(memberId, emitter, unreadCount);
        return emitter;
    }

    @Override
    public void pushUnreadCount(Long receiverId, long unreadCount) {
        for (SseEmitter emitter : emitters.getOrDefault(receiverId, List.of())) {
            send(receiverId, emitter, unreadCount);
        }
    }

    /**
     * 25초마다 빈 신호(주석)를 보낸다. nginx는 60초 동안 아무것도 오가지 않으면 연결을 끊고,
     * 이미 끊긴 연결은 보내다 실패해야 알 수 있어서 이때 정리된다.
     */
    @Scheduled(fixedRate = 25_000)
    public void sendHeartbeat() {
        emitters.forEach((memberId, list) -> list.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().comment("ping"));
            } catch (IOException | IllegalStateException e) {
                remove(memberId, emitter);
            }
        }));
    }

    /** 지금 열린 연결 수 (테스트용) */
    int connectionCount(Long memberId) {
        return emitters.getOrDefault(memberId, List.of()).size();
    }

    private void send(Long memberId, SseEmitter emitter, long unreadCount) {
        try {
            emitter.send(SseEmitter.event().name(UNREAD_COUNT_EVENT).data(new UnreadCountResult(unreadCount)));
        } catch (IOException | IllegalStateException e) {
            // 브라우저가 이미 닫았거나 끝난 연결. 정리만 하고 넘어간다
            log.debug("SSE 전송 실패, 연결 정리 memberId={}", memberId);
            remove(memberId, emitter);
        }
    }

    private void remove(Long memberId, SseEmitter emitter) {
        emitters.computeIfPresent(memberId, (id, list) -> {
            list.remove(emitter);
            return list.isEmpty() ? null : list;
        });
    }
}
