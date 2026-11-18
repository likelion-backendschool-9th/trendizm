package com.trendizm.backend.auth.application.port.out;

import com.trendizm.backend.auth.application.dto.RefreshTokenEntry;
import java.util.Optional;

/**
 * 리프레시 토큰 저장소. 구현체는 adapter/out/redis/RedisRefreshTokenStore.
 * 테스트에서는 Redis 대신 메모리에 저장하는 구현을 쓴다(src/test/.../InMemoryRefreshTokenStore).
 */
public interface RefreshTokenStore {

    /** 새 리프레시 토큰을 만들어 저장하고 돌려준다. 기기마다 따로 발급된다 */
    String issue(Long memberId, boolean rememberMe);

    /** 토큰을 꺼내면서 지운다. 한 번 쓴 토큰은 다시 쓸 수 없다. 없거나 만료됐으면 빈 값 */
    Optional<RefreshTokenEntry> consume(String refreshToken);

    /** 로그아웃 때 지운다. 없으면 아무것도 하지 않는다 */
    void revoke(String refreshToken);
}
