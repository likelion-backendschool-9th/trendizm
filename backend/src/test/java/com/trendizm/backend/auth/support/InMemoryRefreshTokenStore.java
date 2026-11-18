package com.trendizm.backend.auth.support;

import com.trendizm.backend.auth.application.dto.RefreshTokenEntry;
import com.trendizm.backend.auth.application.port.out.RefreshTokenStore;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * 테스트용 리프레시 토큰 저장소. 테스트 환경에는 Redis가 없어서 메모리에 저장한다.
 * src/test에 있는 @Component라 모든 @SpringBootTest에서 RedisRefreshTokenStore 대신 쓰인다(@Primary).
 */
@Primary
@Component
public class InMemoryRefreshTokenStore implements RefreshTokenStore {

    private final Map<String, RefreshTokenEntry> tokens = new ConcurrentHashMap<>();

    @Override
    public String issue(Long memberId, boolean rememberMe) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, new RefreshTokenEntry(memberId, rememberMe));
        return token;
    }

    @Override
    public Optional<RefreshTokenEntry> consume(String refreshToken) {
        return Optional.ofNullable(tokens.remove(refreshToken));
    }

    @Override
    public void revoke(String refreshToken) {
        tokens.remove(refreshToken);
    }
}
