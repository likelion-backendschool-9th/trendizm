package com.trendizm.backend.auth.adapter.out.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 스프링 기본 로그인(아이디·비밀번호 폼)은 쓰지 않는다. 로그인은 /api/auth/login에서 직접 확인한다.
 * 이 빈이 없으면 스프링이 임시 계정을 만들고 로그에 비밀번호를 찍으므로, 항상 실패하는 구현을 둔다.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        throw new UsernameNotFoundException(username);
    }
}
