package com.trendizm.backend.auth.adapter.out.security;

import com.trendizm.backend.auth.config.JwtProperties;
import static org.assertj.core.api.Assertions.assertThat;

import com.trendizm.backend.auth.application.dto.AccessTokenClaims;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private static final String SECRET = "test-only-jwt-secret-not-for-real-use-0123456789";

    private JwtTokenProvider provider(String secret, Duration ttl) {
        return new JwtTokenProvider(new JwtProperties(secret, ttl, Duration.ofDays(14)));
    }

    @Test
    @DisplayName("발급한 토큰을 읽으면 회원 id와 권한이 나온다")
    void 발급_확인() {
        JwtTokenProvider provider = provider(SECRET, Duration.ofMinutes(30));

        String token = provider.createAccessToken(7L, "ADMIN");

        assertThat(provider.parseAccessToken(token)).contains(new AccessTokenClaims(7L, "ADMIN"));
    }

    @Test
    @DisplayName("만료된 토큰은 빈 값")
    void 만료() {
        JwtTokenProvider provider = provider(SECRET, Duration.ofSeconds(-10));

        String token = provider.createAccessToken(7L, "USER");

        assertThat(provider.parseAccessToken(token)).isEmpty();
    }

    @Test
    @DisplayName("다른 비밀키로 서명한 토큰(위조)은 빈 값")
    void 위조() {
        String forged = provider("another-secret-key-another-secret-key-0000", Duration.ofMinutes(30))
                .createAccessToken(7L, "ADMIN");

        assertThat(provider(SECRET, Duration.ofMinutes(30)).parseAccessToken(forged)).isEmpty();
    }

    @Test
    @DisplayName("토큰 형식이 아니면 빈 값")
    void 형식_오류() {
        assertThat(provider(SECRET, Duration.ofMinutes(30)).parseAccessToken("not-a-token")).isEmpty();
    }
}
