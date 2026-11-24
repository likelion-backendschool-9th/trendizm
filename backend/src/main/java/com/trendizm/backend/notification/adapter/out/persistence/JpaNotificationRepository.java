package com.trendizm.backend.notification.adapter.out.persistence;

import com.trendizm.backend.notification.application.port.out.NotificationRepository;
import com.trendizm.backend.notification.domain.entity.Notification;
import com.trendizm.backend.notification.domain.enums.NotificationType;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** NotificationRepository(out 포트)의 메서드를 Spring Data가 구현한다. 서비스는 NotificationRepository만 알고 이 인터페이스는 모른다. */
public interface JpaNotificationRepository extends JpaRepository<Notification, Long>, NotificationRepository {

    @Override
    default int refreshUnread(Long receiverId, NotificationType type, Long interpretationId,
                              String subject, int eventCount, LocalDateTime now) {
        return refreshUnreadByTypeName(receiverId, type.name(), interpretationId, subject, eventCount, now);
    }

    /** type 칼럼이 문자열이라(Notification.type 주석) 이름으로 비교한다. 쿼리로 바로 고쳐서 JPA Auditing이 updated_at을 채우지 않으므로 직접 넣는다 */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Notification n
            set n.eventCount = :eventCount, n.subject = :subject, n.lastEventAt = :now, n.updatedAt = :now
            where n.receiverId = :receiverId and n.type = :type and n.interpretationId = :interpretationId
              and n.readAt is null
            """)
    int refreshUnreadByTypeName(@Param("receiverId") Long receiverId, @Param("type") String type,
                                @Param("interpretationId") Long interpretationId, @Param("subject") String subject,
                                @Param("eventCount") int eventCount, @Param("now") LocalDateTime now);
}
