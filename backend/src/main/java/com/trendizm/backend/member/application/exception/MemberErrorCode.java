package com.trendizm.backend.member.application.exception;

import com.trendizm.backend.global.error.ErrorCode;
import org.springframework.http.HttpStatus;

/** 회원 도메인 에러. 예: throw new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND); */
public enum MemberErrorCode implements ErrorCode {

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입했거나 탈퇴 후 30일이 지나지 않은 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    /** 동시에 같은 값으로 가입해 DB에서 막힌 경우. 이메일인지 닉네임인지 구분할 수 없다 */
    DUPLICATE_MEMBER(HttpStatus.CONFLICT, "이미 사용 중인 이메일 또는 닉네임입니다.");

    private final HttpStatus status;
    private final String message;

    MemberErrorCode(HttpStatus status, String message) {
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
