package com.trendizm.backend.auth.adapter.in.security;

import com.trendizm.backend.auth.adapter.out.security.CustomUserDetails;
import com.trendizm.backend.auth.application.dto.AccessTokenClaims;
import com.trendizm.backend.auth.application.port.out.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * "Authorization: Bearer {액세스 토큰}" 헤더가 올바르면 로그인한 회원(CustomUserDetails)으로 처리한다.
 * 헤더는 Next 서버(BFF)가 access_token 쿠키에서 꺼내 붙인다.
 * 헤더가 없거나 만료·위조면 아무것도 하지 않는다 → 로그인이 필요한 경로면 SecurityConfig에서 401.
 * 서블릿 필터로 두 번 등록되지 않게 @Component를 붙이지 않고 SecurityConfig에서 만든다.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenProvider tokenProvider;

    public JwtAuthenticationFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            String token = header.substring(BEARER_PREFIX.length()).trim();
            if (!token.isEmpty()) {
                tokenProvider.parseAccessToken(token).ifPresent(this::authenticate);
            }
        }
        chain.doFilter(request, response);
    }

    private void authenticate(AccessTokenClaims claims) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + claims.role()));
        CustomUserDetails principal = new CustomUserDetails(
                claims.memberId(), String.valueOf(claims.memberId()), "", authorities);
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal, null, authorities));
    }
}
