package com.trendizm.backend.auth.adapter.in.web.dto;

/** 로그아웃 요청. Next 서버(BFF)가 refresh_token 쿠키 값을 본문에 담아 보낸다. 비어 있어도 204 */
public record LogoutRequest(String refreshToken) {

    /** 실수로 로그에 찍혀도 토큰이 보이지 않게 한다 */
    @Override
    public String toString() {
        return "LogoutRequest[refreshToken=***]";
    }
}
