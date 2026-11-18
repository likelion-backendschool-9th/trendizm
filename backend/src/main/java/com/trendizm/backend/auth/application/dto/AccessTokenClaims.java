package com.trendizm.backend.auth.application.dto;

/** 액세스 토큰에 담긴 값. role은 "USER" 또는 "ADMIN" */
public record AccessTokenClaims(Long memberId, String role) {
}
