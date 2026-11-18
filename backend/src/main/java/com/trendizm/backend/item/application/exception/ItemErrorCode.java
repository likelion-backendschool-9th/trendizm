package com.trendizm.backend.item.application.exception;

import com.trendizm.backend.global.error.ErrorCode;
import org.springframework.http.HttpStatus;

/** 트렌드 아이템 도메인 에러. 예: throw new BusinessException(ItemErrorCode.ITEM_QUESTION_NOT_FOUND); */
public enum ItemErrorCode implements ErrorCode {

    /** 없거나 관리자가 숨긴 질문 글 */
    ITEM_QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "질문 글을 찾을 수 없습니다."),
    ITEM_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
    /** 다른 글의 댓글이거나 대댓글에 답글을 달려고 한 경우 */
    ITEM_INVALID_PARENT_COMMENT(HttpStatus.BAD_REQUEST, "답글을 달 수 없는 댓글입니다."),
    /** 목록 필터의 대분류와 소분류가 서로 맞지 않는 경우 */
    ITEM_CATEGORY_MISMATCH(HttpStatus.BAD_REQUEST, "대분류와 소분류가 맞지 않습니다.");

    private final HttpStatus status;
    private final String message;

    ItemErrorCode(HttpStatus status, String message) {
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
