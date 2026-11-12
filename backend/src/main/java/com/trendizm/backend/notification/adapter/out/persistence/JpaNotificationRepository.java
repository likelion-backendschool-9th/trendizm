package com.trendizm.backend.notification.adapter.out.persistence;

import com.trendizm.backend.notification.application.port.out.NotificationRepository;
import com.trendizm.backend.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

/** NotificationRepository(out 포트)의 메서드를 Spring Data가 구현한다. 서비스는 NotificationRepository만 알고 이 인터페이스는 모른다. */
public interface JpaNotificationRepository extends JpaRepository<Notification, Long>, NotificationRepository {
}
