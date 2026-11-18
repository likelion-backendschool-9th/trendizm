package com.trendizm.backend.auth.adapter.out.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.trendizm.backend.auth.application.dto.RefreshTokenEntry;
import com.trendizm.backend.auth.config.JwtProperties;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/** 실제 Redis 없이 저장소가 Redis에 어떤 키로 저장·조회하는지 확인한다 */
class RedisRefreshTokenStoreTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private RedisRefreshTokenStore store;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        JwtProperties properties = new JwtProperties(
                "test-only-jwt-secret-not-for-real-use-0123456789", Duration.ofMinutes(30), Duration.ofDays(14));
        store = new RedisRefreshTokenStore(redisTemplate, properties);
    }

    @Test
    @DisplayName("토큰 원문이 아니라 SHA-256 해시를 키로 저장하고, 만료 시간은 리프레시 토큰 수명이다")
    void 해시로_저장() {
        String token = store.issue(7L, true);

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(key.capture(), eq("7:true"), eq(Duration.ofDays(14)));
        assertThat(token).matches("^[A-Za-z0-9_-]{43}$");
        assertThat(key.getValue()).startsWith("refresh_token:").doesNotContain(token);
        assertThat(key.getValue().substring("refresh_token:".length())).matches("^[0-9a-f]{64}$");
    }

    @Test
    @DisplayName("돌아온 토큰을 같은 해시 키로 찾아 꺼내고 지운다")
    void 해시로_조회() {
        String token = store.issue(7L, true);
        ArgumentCaptor<String> savedKey = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(savedKey.capture(), anyString(), any(Duration.class));
        given(valueOperations.getAndDelete(savedKey.getValue())).willReturn("7:true");

        assertThat(store.consume(token)).contains(new RefreshTokenEntry(7L, true));
    }

    @Test
    @DisplayName("형식이 다른 토큰(외부에서 조작한 값)은 Redis를 조회하지 않고 거절한다")
    void 형식_검사() {
        assertThat(store.consume("not-a-token")).isEmpty();
        assertThat(store.consume("a".repeat(43) + "=")).isEmpty();
        assertThat(store.consume("한글토큰")).isEmpty();
        assertThat(store.consume(null)).isEmpty();
        store.revoke("../../etc");

        verify(valueOperations, never()).getAndDelete(anyString());
        verify(redisTemplate, never()).delete(anyString());
    }
}
