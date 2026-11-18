package com.trendizm.backend.auth.application.dto;

/**
 * 로그인·가입·재발급 응답 본문. 받는 쪽은 브라우저가 아니라 Next 서버(BFF, frontend/app/api)다.
 * Next 서버가 토큰을 HttpOnly 쿠키로 저장하고, 브라우저에는 member만 돌려준다.
 * - accessTokenExpiresIn, refreshTokenExpiresIn: 남은 수명(초). 쿠키 Max-Age로 쓴다
 * - rememberMe가 false면 refresh 쿠키는 Max-Age 없이(브라우저를 닫으면 사라지게) 저장한다
 */
public record LoginResponse(
        LoginMember member,
        String accessToken,
        long accessTokenExpiresIn,
        String refreshToken,
        long refreshTokenExpiresIn,
        boolean rememberMe
) {

    /** 실수로 로그에 찍혀도 토큰이 보이지 않게 한다 */
    @Override
    public String toString() {
        return "LoginResponse[member=" + member + ", rememberMe=" + rememberMe + "]";
    }
}
