package com.trendizm.backend.global.error;

import lombok.Getter;

/**
 * 서비스에서 예상한 실패(대상 없음, 중복, 권한 없음 등)를 알릴 때 던진다.
 * {@link GlobalExceptionHandler}가 ErrorCode의 상태와 { code, message } 응답으로 바꾼다.
 * 예: {@code throw new BusinessException(MemberErrorCode.NOT_FOUND);}
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    /** ErrorCode의 기본 메시지 대신 상황에 맞는 메시지를 보여 줄 때 쓴다 */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
