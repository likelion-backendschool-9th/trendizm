package com.trendizm.backend.notification.domain.enums;

import java.util.Locale;

/**
 * 알림 종류 (11.1). 종류를 늘릴 때는 여기에 한 줄 추가하고, 이벤트를 받는 리스너(adapter/in/event)와
 * 화면 문구(NotificationResponse.message)를 함께 고친다.
 */
public enum NotificationType {

    /** 내 해석에 동의 투표가 달림. 같은 해석의 안 읽은 알림이 있으면 새로 만들지 않고 그 알림의 동의 수를 고친다 */
    MEME_VOTE(true),

    /** 내가 등록한 밈 문서에 다른 사람이 새 해석을 씀 */
    MEME_INTERPRETATION(false);

    private final boolean grouped;

    NotificationType(boolean grouped) {
        this.grouped = grouped;
    }

    /** 같은 해석의 안 읽은 알림 하나로 묶는 종류인지 */
    public boolean isGrouped() {
        return grouped;
    }

    /** 응답에 쓰는 값. 예: MEME_VOTE → "meme-vote" */
    public String slug() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
