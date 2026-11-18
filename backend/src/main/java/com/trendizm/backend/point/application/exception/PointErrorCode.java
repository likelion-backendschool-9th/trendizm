package com.trendizm.backend.point.application.exception;

import com.trendizm.backend.global.error.ErrorCode;
import org.springframework.http.HttpStatus;

/** 포인트 도메인 에러. 예: throw new BusinessException(PointErrorCode.INSUFFICIENT_POINT_BALANCE); */
public enum PointErrorCode implements ErrorCode {

    INSUFFICIENT_POINT_BALANCE(HttpStatus.CONFLICT, "포인트가 부족합니다."),
    /** 같은 열람 id로 이미 결제한 경우 */
    POINT_ALREADY_PAID(HttpStatus.CONFLICT, "이미 결제한 열람입니다.");

    private final HttpStatus status;
    private final String message;

    PointErrorCode(HttpStatus status, String message) {
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
