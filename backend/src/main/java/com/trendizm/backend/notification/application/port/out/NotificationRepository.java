package com.trendizm.backend.notification.application.port.out;

import com.trendizm.backend.notification.domain.entity.Notification;
import com.trendizm.backend.notification.domain.enums.NotificationType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** 구현체는 adapter/out/persistence/JpaNotificationRepository (Spring Data가 자동 구현) */
public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findByIdAndReceiverId(Long id, Long receiverId);

    /** 목록. 마지막 알림 시각 최신순, 같으면 id 역순 */
    Page<Notification> findByReceiverIdOrderByLastEventAtDescIdDesc(Long receiverId, Pageable pageable);

    long countByReceiverIdAndReadAtIsNull(Long receiverId);

    /**
     * 같은 해석의 안 읽은 알림이 있으면 그 알림의 수(더 클 때만)·단어·마지막 시각을 바꾼다. 바꾼 행 수를 돌려준다.
     * 확인과 수정을 쿼리 하나로 해서, 동시에 들어온 투표가 서로의 수정을 덮어쓰지 않게 한다.
     */
    int refreshUnread(Long receiverId, NotificationType type, Long interpretationId,
                      String subject, int eventCount, LocalDateTime now);
}
