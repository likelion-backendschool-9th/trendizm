package com.trendizm.backend.global.error;

import java.util.List;
import lombok.Getter;

/**
 * 서비스에서 예상한 실패(대상 없음, 중복, 권한 없음 등)를 알릴 때 던진다.
 * {@link GlobalExceptionHandler}가 ErrorCode의 상태와 { code, message } 응답으로 바꾼다.
 * 예: {@code throw new BusinessException(MemberErrorCode.NOT_FOUND);}
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    /** 응답의 errors에 담을 항목별 오류. 없으면 빈 목록 */
    private final List<ErrorResponse.FieldError> errors;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, errorCode.message());
    }

    /** ErrorCode의 기본 메시지 대신 상황에 맞는 메시지를 보여 줄 때 쓴다 */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errors = List.of();
    }

    /** 여러 항목이 틀렸을 때 항목별 오류를 함께 알린다. 예: CSV의 줄마다 틀린 이유 */
    public BusinessException(ErrorCode errorCode, List<ErrorResponse.FieldError> errors) {
        super(errorCode.message());
        this.errorCode = errorCode;
        this.errors = List.copyOf(errors);
    }
}
