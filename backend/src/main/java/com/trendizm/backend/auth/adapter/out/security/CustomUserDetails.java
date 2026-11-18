package com.trendizm.backend.auth.adapter.out.security;

import java.util.Collection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 로그인한 회원(principal). JwtAuthenticationFilter가 Authorization 헤더의 액세스 토큰을 확인하고 만든다.
 * 다른 도메인은 이 클래스를 참조하지 않고 컨트롤러에서 id만 받는다(AGENTS.md).
 * 예: public MyResponse me(@AuthenticationPrincipal(expression = "id") Long userId)
 * 토큰에는 비밀번호가 없으므로 password는 빈 문자열, username은 회원 id 문자열이다.
 */
@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
}
