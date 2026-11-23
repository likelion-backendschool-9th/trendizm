package com.trendizm.backend.lookup.application.exception;

import com.trendizm.backend.global.error.ErrorCode;
import org.springframework.http.HttpStatus;

/** 밈 검색 도메인 에러. 예: throw new BusinessException(MemeErrorCode.MEME_NOT_FOUND); */
public enum MemeErrorCode implements ErrorCode {

    /** 문서가 없거나 숨김 상태. 숨김 문서도 없는 문서와 같은 응답을 준다 */
    MEME_NOT_FOUND(HttpStatus.NOT_FOUND, "밈 문서를 찾을 수 없습니다."),

    /** normalizeWord로 바꾼 단어가 word 칼럼 길이(100자)를 넘음. 예: "İ"는 소문자로 바꾸면 2글자가 된다 */
    MEME_WORD_TOO_LONG(HttpStatus.BAD_REQUEST, "단어가 너무 깁니다. 100자 이하로 입력해 주세요."),

    /** 정규화한 출처 주소가 source_url 칼럼 길이(500자)를 넘음. 예: http를 https로 바꾸면 한 글자 늘어난다 */
    MEME_SOURCE_URL_TOO_LONG(HttpStatus.BAD_REQUEST, "출처 주소가 너무 깁니다."),

    /** 같은 단어·출처로 거의 동시에 문서가 만들어져 (word, source_url) UNIQUE에 막힘. 다시 시도하면 만들어진 문서에 의견이 붙는다 */
    MEME_ALREADY_EXISTS(HttpStatus.CONFLICT, "같은 단어의 문서가 방금 만들어졌습니다. 다시 시도해 주세요.");

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
