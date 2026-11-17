package com.trendizm.backend.global.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * 모든 실패 응답의 본문. 예: { "code": "MEMBER_NOT_FOUND", "message": "회원을 찾을 수 없습니다." }
 * 입력값 검증 실패일 때만 errors에 필드별 오류가 붙는다.
 * 예: { "code": "INVALID_INPUT", "message": "...", "errors": [ { "field": "email", "message": "이메일 형식이 아닙니다." } ] }
 * 스프링에도 같은 이름의 org.springframework.web.ErrorResponse가 있으니 import할 때 이 패키지인지 확인한다.
 */
public record ErrorResponse(
        String code,
        String message,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) List<FieldError> errors
) {

    /** 어떤 필드가 왜 틀렸는지 */
    public record FieldError(String field, String message) {
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.code(), errorCode.message(), List.of());
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.code(), message, List.of());
    }

    public static ErrorResponse of(ErrorCode errorCode, List<FieldError> errors) {
        return new ErrorResponse(errorCode.code(), errorCode.message(), errors);
    }
}
