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

    /**
     * type 칼럼이 문자열이라(Notification.type 주석) 이름으로 비교한다. 쿼리로 바로 고쳐서 JPA Auditing이 updated_at을 채우지 않으므로 직접 넣는다.
     * 수는 줄이지 않는다. 동시에 들어온 동의는 커밋된 순서와 알림이 처리되는 순서가 다를 수 있어서, 늦게 처리된 옛 수(14)가
     * 새 수(15)를 덮어쓰지 않게 한다. 동의 취소·비동의는 알림을 만들지 않으므로 수가 줄어드는 경우는 없다.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Notification n
            set n.eventCount = case when :eventCount > n.eventCount then :eventCount else n.eventCount end,
                n.subject = :subject, n.lastEventAt = :now, n.updatedAt = :now
            where n.receiverId = :receiverId and n.type = :type and n.interpretationId = :interpretationId
              and n.readAt is null
            """)
    int refreshUnreadByTypeName(@Param("receiverId") Long receiverId, @Param("type") String type,
                                @Param("interpretationId") Long interpretationId, @Param("subject") String subject,
                                @Param("eventCount") int eventCount, @Param("now") LocalDateTime now);
}
