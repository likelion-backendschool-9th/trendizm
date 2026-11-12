package com.trendizm.backend.auth.adapter.out.security;

import java.util.Collection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Spring Security가 인증에 쓰는 사용자 정보.
 * users 테이블의 컬럼(로그인 아이디, 비밀번호 해시)이 정해지면 CustomUserDetailsService에서 채운다.
 */
@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
}
