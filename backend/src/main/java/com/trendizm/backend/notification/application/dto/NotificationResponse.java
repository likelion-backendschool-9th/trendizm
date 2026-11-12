package com.trendizm.backend.notification.application.dto;

import com.trendizm.backend.notification.domain.Notification;

/** 컬럼이 정해지면 응답 필드를 추가한다. */
public record NotificationResponse(Long id) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(notification.getId());
    }
}
