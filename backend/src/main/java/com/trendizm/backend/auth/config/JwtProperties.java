package com.trendizm.backend.auth.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * application.yml의 trendizm.jwt.* 설정. 토큰 발급(adapter.out)과 AuthService(응답의 만료 시간)가 함께 쓰므로 adapter 밖(config)에 둔다.
 * 쿠키는 Next 서버(BFF)가 저장하므로 쿠키 설정은 여기 없다.
 * <pre>
 * trendizm:
 *   jwt:
 *     secret: ${JWT_SECRET}      # 필수. 32자 이상 임의 문자열. 커밋하지 않는다
 *     access-token-ttl: 30m      # 생략하면 30분
 *     refresh-token-ttl: 14d     # 생략하면 14일
 * </pre>
 */
@Validated
@ConfigurationProperties("trendizm.jwt")
public record JwtProperties(
        @NotBlank @Size(min = 32, message = "trendizm.jwt.secret은 32자 이상이어야 합니다.") String secret,
        @DefaultValue("30m") Duration accessTokenTtl,
        @DefaultValue("14d") Duration refreshTokenTtl
) {

    /** 실수로 로그에 찍혀도 비밀키가 보이지 않게 한다 */
    @Override
    public String toString() {
        return "JwtProperties[accessTokenTtl=" + accessTokenTtl + ", refreshTokenTtl=" + refreshTokenTtl + "]";
    }
}
