package com.trendizm.backend.auth.application.exception;

import com.trendizm.backend.global.error.ErrorCode;
import org.springframework.http.HttpStatus;

public enum AuthErrorCode implements ErrorCode {

    /** 이메일이 없을 때와 비밀번호가 틀릴 때를 구분하지 않는다(가입 여부가 드러나지 않게) */
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "로그인이 만료되었습니다. 다시 로그인해 주세요."),
    /** 관리자가 이용을 제한한 회원. 로그인할 때는 비밀번호가 맞을 때만 이 코드를 준다(제한 여부가 드러나지 않게) */
    MEMBER_SUSPENDED(HttpStatus.FORBIDDEN, "이용이 제한된 계정입니다.");

    private final HttpStatus status;
    private final String message;

    AuthErrorCode(HttpStatus status, String message) {
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
