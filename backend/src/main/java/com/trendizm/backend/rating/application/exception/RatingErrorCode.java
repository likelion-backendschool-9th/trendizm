package com.trendizm.backend.rating.application.exception;

import com.trendizm.backend.global.error.ErrorCode;
import org.springframework.http.HttpStatus;

/** 호불호 평점 도메인 에러. 예: throw new BusinessException(RatingErrorCode.RATING_CONTENT_NOT_FOUND); */
public enum RatingErrorCode implements ErrorCode {

    /** 없거나 관리자가 숨긴 콘텐츠 */
    RATING_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "콘텐츠를 찾을 수 없습니다."),
    /** 없는 카테고리 slug */
    RATING_INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "없는 카테고리입니다."),
    /** 없는 진행 상태 slug */
    RATING_INVALID_STATUS(HttpStatus.BAD_REQUEST, "없는 진행 상태입니다."),
    /** 추가 정보에 없는 키나 그 소분류에서 쓸 수 없는 키가 있는 경우 (RatingContentExtraKey) */
    RATING_INVALID_EXTRA_KEY(HttpStatus.BAD_REQUEST, "이 콘텐츠에 넣을 수 없는 추가 정보가 있습니다."),
    /** 없는 호/불호 slug (like·dislike가 아님) */
    RATING_INVALID_SENTIMENT(HttpStatus.BAD_REQUEST, "좋아요/싫어요 값이 올바르지 않습니다."),
    /** 없는 후기 정렬 slug */
    RATING_INVALID_SORT(HttpStatus.BAD_REQUEST, "없는 정렬 방식입니다."),
    /** 목록 필터의 대분류와 소분류가 서로 맞지 않는 경우 */
    RATING_CATEGORY_MISMATCH(HttpStatus.BAD_REQUEST, "대분류와 소분류가 맞지 않습니다.");

    private final HttpStatus status;
    private final String message;

    RatingErrorCode(HttpStatus status, String message) {
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
