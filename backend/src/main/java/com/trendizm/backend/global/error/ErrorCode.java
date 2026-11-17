package com.trendizm.backend.global.error;

import org.springframework.http.HttpStatus;

/**
 * 에러 코드의 공통 모양. 실패 응답은 {@link ErrorResponse} 형식({ code, message })으로 나간다.
 * 공통 에러는 {@link CommonErrorCode}, 도메인 에러는 도메인마다 XxxErrorCode enum으로 만든다.
 * 예: {@code MemberErrorCode.NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다.")}
 */
public interface ErrorCode {

    /** 응답 HTTP 상태 */
    HttpStatus status();

    /** 응답 code 값. 보통 enum 상수 이름을 그대로 쓴다 (예: MEMBER_NOT_FOUND) */
    String code();

    /** 사용자에게 보여 줄 기본 메시지 */
    String message();
}
