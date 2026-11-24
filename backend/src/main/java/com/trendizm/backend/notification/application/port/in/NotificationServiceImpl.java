package com.trendizm.backend.notification.application.port.in;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.member.application.port.MemberService;
import com.trendizm.backend.notification.application.dto.CreateNotificationCommand;
import com.trendizm.backend.notification.application.dto.NotificationResponse;
import com.trendizm.backend.notification.application.exception.NotificationErrorCode;
import com.trendizm.backend.notification.application.port.NotificationService;
import com.trendizm.backend.notification.application.port.out.NotificationPusher;
import com.trendizm.backend.notification.application.port.out.NotificationRepository;
import com.trendizm.backend.notification.domain.entity.Notification;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPusher notificationPusher;
    private final MemberService memberService;

    @Override
    public PageResponse<NotificationResponse> getNotifications(Long receiverId, int page, int size) {
        return PageResponse.from(
                notificationRepository.findByReceiverIdOrderByLastEventAtDescIdDesc(receiverId, PageRequest.of(page, size)),
                NotificationResponse::from);
    }

    @Override
    public long getUnreadCount(Long receiverId) {
        return notificationRepository.countByReceiverIdAndReadAtIsNull(receiverId);
    }

    @Override
    public SseEmitter subscribe(Long receiverId) {
        return notificationPusher.subscribe(receiverId, getUnreadCount(receiverId));
    }

    /** 다른 탭에 열린 헤더의 숫자도 줄어들게, 읽음으로 바뀌었으면 커밋 뒤에 안 읽은 수를 보낸다 */
    @Override
    @Transactional
    public void markAsRead(Long receiverId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndReceiverId(notificationId, receiverId)
                .orElseThrow(() -> new BusinessException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));
        if (notification.markAsRead(now())) {
            pushUnreadCountAfterCommit(receiverId);
        }
    }

    /**
     * 투표·해석 작성이 커밋된 뒤(AFTER_COMMIT)에 불린다. 그때는 원래 트랜잭션이 이미 끝나서
     * 같은 트랜잭션에 참여하면 저장이 커밋되지 않으므로 REQUIRES_NEW로 새 트랜잭션을 연다.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notify(CreateNotificationCommand command) {
        Long receiverId = command.receiverId();
        if (receiverId.equals(command.actorId()) || !memberService.isNotificationEnabled(receiverId)) {
            return;
        }
        LocalDateTime now = now();
        String subject = Notification.truncateSubject(command.subject());
        boolean refreshed = command.type().isGrouped()
                && notificationRepository.refreshUnread(receiverId, command.type(), command.interpretationId(),
                subject, command.eventCount(), now) > 0;
        if (!refreshed) {
            notificationRepository.save(Notification.create(receiverId, command.type(), command.memeId(),
                    command.interpretationId(), subject, command.eventCount(), now));
        }
        pushUnreadCountAfterCommit(receiverId);
    }

    /**
     * 저장이 커밋되기 전에 보내면 화면이 목록을 다시 불렀을 때 새 알림이 아직 없을 수 있다. 그래서 커밋 뒤에 보낸다.
     * 실시간 전송은 덤이라, 실패해도 로그만 남기고 저장·읽음 처리 결과에는 영향을 주지 않는다.
     */
    private void pushUnreadCountAfterCommit(Long receiverId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    notificationPusher.pushUnreadCount(receiverId,
                            notificationRepository.countByReceiverIdAndReadAtIsNull(receiverId));
                } catch (RuntimeException e) {
                    log.warn("안 읽은 알림 수 전송 실패 receiverId={}", receiverId, e);
                }
            }
        });
    }

    /** JPA Auditing(JpaAuditingConfig)과 같게 마이크로초까지만 쓴다. PostgreSQL timestamp 정밀도와 맞춘다 */
    private static LocalDateTime now() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
    }
}
