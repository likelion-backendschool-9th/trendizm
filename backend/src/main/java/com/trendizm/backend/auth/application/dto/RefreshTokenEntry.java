package com.trendizm.backend.auth.application.dto;

/** 리프레시 토큰에 저장해 둔 값. rememberMe는 다시 발급할 때도 "로그인 상태 유지"를 이어 가려고 저장한다 */
public record RefreshTokenEntry(Long memberId, boolean rememberMe) {
}
