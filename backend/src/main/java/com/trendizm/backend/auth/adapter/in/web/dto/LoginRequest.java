package com.trendizm.backend.auth.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** 로그인 요청. rememberMe는 화면의 "로그인 상태 유지" 체크박스 */
public record LoginRequest(
        @NotBlank(message = "이메일을 입력해 주세요.")
        @Email(message = "이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "비밀번호를 입력해 주세요.")
        String password,

        boolean rememberMe
) {

    /** 실수로 로그에 찍혀도 비밀번호가 보이지 않게 한다 */
    @Override
    public String toString() {
        return "LoginRequest[email=" + email + ", rememberMe=" + rememberMe + "]";
    }
}
