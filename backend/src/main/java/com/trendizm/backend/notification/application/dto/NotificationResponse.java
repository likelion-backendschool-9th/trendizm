package com.trendizm.backend.notification.application.dto;

import com.trendizm.backend.notification.domain.entity.Notification;
import com.trendizm.backend.notification.domain.enums.NotificationType;
import java.time.LocalDateTime;

/**
 * 알림 목록 한 줄. 화면 주소(link)는 담지 않는다. 밈 화면 주소가 바뀌어도 알림이 깨지지 않게,
 * 화면이 type·memeId·interpretationId로 주소를 만든다.
 *
 * @param type "meme-vote"(내 해석에 동의) 또는 "meme-interpretation"(내 문서에 새 해석)
 * @param count meme-vote면 그 해석의 동의 수, meme-interpretation이면 1
 * @param occurredAt 알림이 생기거나 묶인 마지막 시각. 목록은 이 값의 최신순이다
 */
public record NotificationResponse(
        Long id,
        String type,
        Long memeId,
        Long interpretationId,
        String subject,
        int count,
        String message,
        boolean read,
        LocalDateTime occurredAt
) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType().slug(),
                notification.getMemeId(),
                notification.getInterpretationId(),
                notification.getSubject(),
                notification.getEventCount(),
                message(notification),
                notification.isRead(),
                notification.getLastEventAt());
    }

    /** 문구는 저장하지 않고 조회할 때 만든다. 동의 수가 바뀌면 문구도 따라 바뀐다 */
    private static String message(Notification notification) {
        // enum switch는 익명 클래스(NotificationResponse$1)를 만들어 'DTO는 record' 규칙(ArchitectureTest)에 걸려서 if로 쓴다
        String subject = notification.getSubject();
        if (notification.getType() == NotificationType.MEME_VOTE) {
            return "‘%s’에 쓴 해석에 동의 %d개가 달렸어요.".formatted(subject, notification.getEventCount());
        }
        return "‘%s’에 새 해석이 달렸어요.".formatted(subject);
    }
}
