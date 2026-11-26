package com.trendizm.backend.accuse.application.exception;

import com.trendizm.backend.global.error.ErrorCode;
import org.springframework.http.HttpStatus;

/** 신고 도메인 에러. 예: throw new BusinessException(AccuseErrorCode.ACCUSE_ALREADY_EXISTS); */
public enum AccuseErrorCode implements ErrorCode {

    /** 신고 사유가 sexual, hate, ad, etc가 아님 */
    ACCUSE_INVALID_REASON(HttpStatus.BAD_REQUEST, "신고 사유는 sexual, hate, ad, etc 중 하나여야 합니다."),

    /** 사유가 기타인데 신고 내용이 비어 있음 */
    ACCUSE_DESCRIPTION_REQUIRED(HttpStatus.BAD_REQUEST, "기타 사유로 신고할 때는 신고 내용을 입력해 주세요."),

    /** 신고 내용이 description 칼럼 길이(500자)를 넘음 */
    ACCUSE_DESCRIPTION_TOO_LONG(HttpStatus.BAD_REQUEST, "신고 내용은 500자 이하여야 합니다."),

    /** 같은 사람이 같은 대상을 이미 신고함. 처리된 뒤에도 다시 신고할 수 없다 */
    ACCUSE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 신고한 대상입니다.");

    private final HttpStatus status;
    private final String message;

    AccuseErrorCode(HttpStatus status, String message) {
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
