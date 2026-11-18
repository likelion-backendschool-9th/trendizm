package com.trendizm.backend.auth.adapter.out.security;

import com.trendizm.backend.auth.config.JwtProperties;
import com.trendizm.backend.auth.application.dto.AccessTokenClaims;
import com.trendizm.backend.auth.application.port.out.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;

/**
 * 액세스 토큰(JWT, HS256). subject = 회원 id, role 클레임 = USER/ADMIN.
 * SecurityConfig가 @Import로 등록한다(@Component를 붙이지 않는다). 그래야 컨트롤러 테스트에서 @Import(SecurityConfig.class) 하나로 보안 설정이 갖춰진다.
 */
public class JwtTokenProvider implements TokenProvider {

    private static final String ROLE_CLAIM = "role";

    private final SecretKey key;
    private final Duration accessTokenTtl;

    public JwtTokenProvider(JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtl = properties.accessTokenTtl();
    }

    @Override
    public String createAccessToken(Long memberId, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim(ROLE_CLAIM, role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenTtl.toMillis()))
                .signWith(key)
                .compact();
    }

    @Override
    public Optional<AccessTokenClaims> parseAccessToken(String accessToken) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(accessToken)
                    .getPayload();
            return Optional.of(new AccessTokenClaims(Long.valueOf(claims.getSubject()), claims.get(ROLE_CLAIM, String.class)));
        } catch (JwtException | IllegalArgumentException e) {
            // 만료, 서명 불일치, 형식 오류 → 로그인하지 않은 요청으로 본다
            return Optional.empty();
        }
    }
}
