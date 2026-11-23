package com.trendizm.backend.lookup.application.exception;

import com.trendizm.backend.global.error.ErrorCode;
import org.springframework.http.HttpStatus;

/** 밈 검색 도메인 에러. 예: throw new BusinessException(MemeErrorCode.MEME_NOT_FOUND); */
public enum MemeErrorCode implements ErrorCode {

    /** 문서가 없거나 숨김 상태. 숨김 문서도 없는 문서와 같은 응답을 준다 */
    MEME_NOT_FOUND(HttpStatus.NOT_FOUND, "밈 문서를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    MemeErrorCode(HttpStatus status, String message) {
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
