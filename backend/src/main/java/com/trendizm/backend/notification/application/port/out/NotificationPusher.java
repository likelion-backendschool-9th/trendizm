package com.trendizm.backend.notification.application.port.out;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 연결된 화면에 실시간으로 알리기 (11.7). 구현체는 adapter/out/sse/SseNotificationPusher.
 * 화면에는 알림 내용 대신 안 읽은 수만 보낸다. 목록은 화면이 열 때 API로 다시 불러서, 재연결 중 놓친 알림을 따로 챙기지 않아도 된다.
 */
public interface NotificationPusher {

    /** 새 연결을 열고 첫 이벤트로 unreadCount를 보낸다 */
    SseEmitter subscribe(Long receiverId, long unreadCount);

    /** 그 회원의 열린 연결 모두에 안 읽은 수를 보낸다. 연결이 없으면 아무것도 하지 않는다 */
    void pushUnreadCount(Long receiverId, long unreadCount);
}
