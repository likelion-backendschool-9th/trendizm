package com.trendizm.backend.notification.application.exception;

import com.trendizm.backend.global.error.ErrorCode;
import org.springframework.http.HttpStatus;

/** 알림 도메인 에러. 예: throw new BusinessException(NotificationErrorCode.NOTIFICATION_NOT_FOUND); */
public enum NotificationErrorCode implements ErrorCode {

    /** 알림이 없거나 다른 회원의 알림. 남의 알림이 있는지 알 수 없게 둘 다 404로 응답한다 */
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    NotificationErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public HttpStatus status() {
        return status;
    }

    @Override
    public String code() {
        return name();
    }

    @Override
    public String message() {
        return message;
    }
}
