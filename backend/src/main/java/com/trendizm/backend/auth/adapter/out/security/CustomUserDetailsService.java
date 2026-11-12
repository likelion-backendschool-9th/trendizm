package com.trendizm.backend.auth.adapter.out.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO(auth): users 테이블에서 username으로 회원을 찾아 CustomUserDetails로 감싸 반환
        //  없으면 throw new UsernameNotFoundException(username)
        throw new UsernameNotFoundException(username);
    }
}
