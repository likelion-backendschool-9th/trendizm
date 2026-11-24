package com.trendizm.backend.notification.adapter.in.event;

import com.trendizm.backend.lookup.application.dto.MemeInterpretationAddedEvent;
import com.trendizm.backend.lookup.application.dto.MemeInterpretationVotedEvent;
import com.trendizm.backend.notification.application.dto.CreateNotificationCommand;
import com.trendizm.backend.notification.application.port.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 밈 검색(lookup)에서 일어난 일을 알림으로 만든다 (11.4).
 * - 투표·해석 작성이 커밋된 뒤에만 실행한다(@TransactionalEventListener 기본값 AFTER_COMMIT). 롤백되면 알림도 없다.
 * - 알림을 만들다 실패해도 예외를 밖으로 던지지 않는다. 이미 커밋된 투표 요청에 500이 나가지 않게 로그만 남긴다.
 * - lookup은 알림 도메인을 모르고 이벤트만 발행한다. 그래서 lookup → notification 의존이 생기지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookupNotificationListener {

    private final NotificationService notificationService;

    @TransactionalEventListener
    public void onVoted(MemeInterpretationVotedEvent event) {
        notifySafely(CreateNotificationCommand.memeVote(event.authorId(), event.voterId(), event.memeId(),
                event.interpretationId(), event.word(), event.agreeCount()));
    }

    @TransactionalEventListener
    public void onInterpretationAdded(MemeInterpretationAddedEvent event) {
        notifySafely(CreateNotificationCommand.memeInterpretation(event.memeOwnerId(), event.authorId(),
                event.memeId(), event.interpretationId(), event.word()));
    }

    private void notifySafely(CreateNotificationCommand command) {
        try {
            notificationService.notify(command);
        } catch (RuntimeException e) {
            log.warn("알림 저장 실패 type={} receiverId={}", command.type(), command.receiverId(), e);
        }
    }
}
