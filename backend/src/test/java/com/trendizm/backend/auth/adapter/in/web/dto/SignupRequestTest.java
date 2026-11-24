package com.trendizm.backend.auth.adapter.in.web.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SignupRequestTest {

    @Test
    @DisplayName("toString에는 비밀번호가 나오지 않는다 (로그에 찍혀도 안전하게)")
    void toString에_비밀번호를_가린다() {
        SignupRequest request = new SignupRequest("a@test.com", "secret-password-123", "닉네임", true, true, false);

        assertThat(request.toString())
                .doesNotContain("secret-password-123")
                .contains("a@test.com", "password=***", "닉네임");
    }
}
