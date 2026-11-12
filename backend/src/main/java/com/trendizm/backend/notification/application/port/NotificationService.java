package com.trendizm.backend.notification.application.port;

import com.trendizm.backend.notification.application.dto.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** 컬럼이 정해지면 createNotification·updateNotification에 파라미터를 추가한다. */
public interface NotificationService {

    Page<NotificationResponse> getNotifications(Pageable pageable);

    /** 없으면 예외를 던진다 */
    NotificationResponse getNotification(Long notificationId);

    /** @return 생성한 알림 id */
    Long createNotification();

    void updateNotification(Long notificationId);

    void deleteNotification(Long notificationId);
}
