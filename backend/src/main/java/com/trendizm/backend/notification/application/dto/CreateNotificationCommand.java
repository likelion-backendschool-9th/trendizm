package com.trendizm.backend.notification.application.dto;

import com.trendizm.backend.notification.domain.enums.NotificationType;

/**
 * 알림 만들기. 다른 도메인 이벤트를 받는 리스너(adapter/in/event)가 이벤트를 이 명령으로 바꿔 넘긴다.
 *
 * @param receiverId 알림을 받는 회원
 * @param actorId 알림을 일으킨 회원(투표한 사람, 해석을 쓴 사람). receiverId와 같으면 알림을 만들지 않는다
 * @param eventCount MEME_VOTE면 그 해석의 현재 동의 수, MEME_INTERPRETATION이면 1
 */
public record CreateNotificationCommand(
        NotificationType type,
        Long receiverId,
        Long actorId,
        Long memeId,
        Long interpretationId,
        String subject,
        int eventCount
) {

    public static CreateNotificationCommand memeVote(Long receiverId, Long voterId, Long memeId, Long interpretationId,
                                                     String word, int agreeCount) {
        return new CreateNotificationCommand(NotificationType.MEME_VOTE, receiverId, voterId, memeId, interpretationId,
                word, agreeCount);
    }

    public static CreateNotificationCommand memeInterpretation(Long receiverId, Long authorId, Long memeId,
                                                               Long interpretationId, String word) {
        return new CreateNotificationCommand(NotificationType.MEME_INTERPRETATION, receiverId, authorId, memeId,
                interpretationId, word, 1);
    }
}
