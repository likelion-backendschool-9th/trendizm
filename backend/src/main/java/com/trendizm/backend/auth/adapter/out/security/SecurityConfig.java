package com.trendizm.backend.auth.adapter.out.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 보안 설정 뼈대. 접근 규칙(위에서부터 먼저 맞는 규칙이 적용된다):
 * 1. /api/auth/** (회원가입·로그인) → 누구나
 * 2. feed·item·lookup·ranking·rating → 누구나 (모든 메서드)
 * 3. 그 외(notification 등) → 로그인 필요
 *
 * TODO(auth): 로그인 방식이 정해지면 인증 필터(JWT 등)를 addFilterBefore로 추가하고,
 *  401·403 응답 형식은 exceptionHandling(...)으로, CORS는 cors(...)로 설정한다.
 *  지금은 인증 수단이 없어서 3번 경로는 항상 401(403)이다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(
                                "/api/feeds/**", "/api/items/**", "/api/lookup/**",
                                "/api/rankings/**", "/api/ratings/**").permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
