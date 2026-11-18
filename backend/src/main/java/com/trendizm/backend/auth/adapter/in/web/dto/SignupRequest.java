package com.trendizm.backend.auth.adapter.in.web.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 요청. 비밀번호 확인 칸은 프론트에서만 비교한다.
 * 비밀번호 최대 64자: BCrypt는 72바이트 뒤를 무시하므로 그보다 짧게 제한한다.
 */
public record SignupRequest(
        @NotBlank(message = "이메일을 입력해 주세요.")
        @Email(message = "이메일 형식이 아닙니다.")
        @Size(max = 100, message = "이메일은 100자 이하여야 합니다.")
        String email,

        @NotBlank(message = "비밀번호를 입력해 주세요.")
        @Size(min = 8, max = 64, message = "비밀번호는 8자 이상 64자 이하여야 합니다.")
        String password,

        @NotBlank(message = "닉네임을 입력해 주세요.")
        @Size(min = 2, max = 30, message = "닉네임은 2자 이상 30자 이하여야 합니다.")
        @Pattern(regexp = "^\\S+$", message = "닉네임에는 공백을 넣을 수 없습니다.")
        String nickname,

        @AssertTrue(message = "이용약관에 동의해 주세요.")
        boolean agreeTerms,

        @AssertTrue(message = "개인정보 처리방침에 동의해 주세요.")
        boolean agreePrivacy,

        boolean marketingAgreed
) {
}
