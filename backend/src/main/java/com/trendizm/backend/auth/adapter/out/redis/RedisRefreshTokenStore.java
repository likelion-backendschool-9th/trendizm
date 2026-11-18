package com.trendizm.backend.auth.adapter.out.redis;

import com.trendizm.backend.auth.application.dto.RefreshTokenEntry;
import com.trendizm.backend.auth.application.port.out.RefreshTokenStore;
import com.trendizm.backend.auth.config.JwtProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis에 "refresh_token:{토큰의 SHA-256}" → "{회원 id}:{rememberMe}"로 저장한다. 만료 시간은 trendizm.jwt.refresh-token-ttl.
 * - 토큰은 추측할 수 없는 임의 32바이트를 Base64 URL로 인코딩한 문자열이다(JWT 아님).
 * - 요청으로 돌아온 토큰은 외부 입력이므로 디코딩하지 않는다(AGENTS.md: 요청 값을 JDK Base64 디코더로 디코딩하지 않는다).
 *   형식만 확인하고 문자열 그대로 해시해서 찾는다.
 * - 토큰 원문 대신 해시를 저장하므로 Redis 내용이 새어 나가도 그 값으로는 재발급할 수 없다.
 */
@Component
@RequiredArgsConstructor
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private static final String KEY_PREFIX = "refresh_token:";
    private static final SecureRandom RANDOM = new SecureRandom();
    /** 32바이트를 패딩 없는 Base64 URL로 인코딩하면 43자다 */
    private static final Pattern TOKEN_FORMAT = Pattern.compile("^[A-Za-z0-9_-]{43}$");

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties properties;

    @Override
    public String issue(Long memberId, boolean rememberMe) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        redisTemplate.opsForValue().set(key(token), memberId + ":" + rememberMe, properties.refreshTokenTtl());
        return token;
    }

    @Override
    public Optional<RefreshTokenEntry> consume(String refreshToken) {
        if (!isWellFormed(refreshToken)) {
            return Optional.empty();
        }
        String value = redisTemplate.opsForValue().getAndDelete(key(refreshToken));
        if (value == null) {
            return Optional.empty();
        }
        String[] parts = value.split(":");
        return Optional.of(new RefreshTokenEntry(Long.valueOf(parts[0]), Boolean.parseBoolean(parts[1])));
    }

    @Override
    public void revoke(String refreshToken) {
        if (isWellFormed(refreshToken)) {
            redisTemplate.delete(key(refreshToken));
        }
    }

    private static boolean isWellFormed(String refreshToken) {
        return refreshToken != null && TOKEN_FORMAT.matcher(refreshToken).matches();
    }

    private static String key(String refreshToken) {
        return KEY_PREFIX + sha256Hex(refreshToken);
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.US_ASCII));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", e);
        }
    }
}
