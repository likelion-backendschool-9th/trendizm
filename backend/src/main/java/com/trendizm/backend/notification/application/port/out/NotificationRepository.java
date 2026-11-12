package com.trendizm.backend.notification.application.port.out;

import com.trendizm.backend.notification.domain.Notification;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 구현체는 adapter/out/persistence/JpaNotificationRepository (Spring Data가 자동 구현).
 * 메서드 이름을 Spring Data 규칙(findByXxx 등)대로 추가하면 쿼리를 따로 쓰지 않아도 된다.
 */
public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(Long id);

    Page<Notification> findAll(Pageable pageable);

    void delete(Notification notification);
}
