package com.trendizm.backend.auth.application.port.out;

import com.trendizm.backend.auth.application.dto.AccessTokenClaims;
import java.util.Optional;

/** 액세스 토큰 발급·확인. 구현체는 adapter/out/security/JwtTokenProvider (JWT) */
public interface TokenProvider {

    String createAccessToken(Long memberId, String role);

    /** 서명이 틀리거나 만료됐으면 빈 값 */
    Optional<AccessTokenClaims> parseAccessToken(String accessToken);
}
