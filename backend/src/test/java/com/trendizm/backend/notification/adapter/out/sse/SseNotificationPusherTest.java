package com.trendizm.backend.notification.adapter.out.sse;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class SseNotificationPusherTest {

    private final SseNotificationPusher sseEmitters = new SseNotificationPusher();

    @Test
    @DisplayName("한 회원의 연결은 5개까지. 넘으면 가장 오래된 연결을 닫는다")
    void 연결_수_제한() {
        List<SseEmitter> opened = new ArrayList<>();
        for (int i = 0; i < SseNotificationPusher.MAX_CONNECTIONS_PER_MEMBER + 2; i++) {
            opened.add(sseEmitters.subscribe(1L, 0));
        }

        assertThat(sseEmitters.connectionCount(1L)).isEqualTo(SseNotificationPusher.MAX_CONNECTIONS_PER_MEMBER);
        assertThat(sseEmitters.connectionCount(2L)).isZero();
    }

    @Test
    @DisplayName("연결이 없는 회원에게 보내도 아무 일도 없다")
    void 연결_없음() {
        sseEmitters.pushUnreadCount(99L, 1);
        sseEmitters.sendHeartbeat();

        assertThat(sseEmitters.connectionCount(99L)).isZero();
    }
}
