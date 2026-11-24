package com.trendizm.backend.notification.application.port;

import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.notification.application.dto.CreateNotificationCommand;
import com.trendizm.backend.notification.application.dto.NotificationResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** 알림 유스케이스 (in 포트). 조회·읽음 처리는 로그인한 회원 본인의 알림만 다룬다 */
public interface NotificationService {

    /** 내 알림 목록. 마지막으로 생기거나 묶인 시각의 최신순 */
    PageResponse<NotificationResponse> getNotifications(Long receiverId, int page, int size);

    /** 안 읽은 알림 수. 묶인 알림도 1개로 센다 */
    long getUnreadCount(Long receiverId);

    /**
     * 실시간 알림 연결(SSE)을 연다. 연결되자마자, 그리고 안 읽은 수가 바뀔 때마다 "unread-count" 이벤트로 { "count": n }을 보낸다.
     */
    SseEmitter subscribe(Long receiverId);

    /** 읽음 처리. 없거나 남의 알림이면 NOTIFICATION_NOT_FOUND. 이미 읽었으면 아무것도 바꾸지 않는다 */
    void markAsRead(Long receiverId, Long notificationId);

    /**
     * 알림 만들기. 받는 회원이 알림을 일으킨 본인이거나, 알림 수신을 껐거나(users.notify = false),
     * 탈퇴·이용 제한 상태면 만들지 않는다. 다른 작업이 커밋된 뒤에 부르므로 새 트랜잭션에서 저장한다.
     */
    void notify(CreateNotificationCommand command);
}
