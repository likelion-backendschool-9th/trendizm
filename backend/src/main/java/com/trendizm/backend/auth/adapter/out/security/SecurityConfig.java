package com.trendizm.backend.auth.adapter.out.security;

import com.trendizm.backend.auth.adapter.in.security.JwtAuthenticationFilter;
import com.trendizm.backend.auth.application.port.out.TokenProvider;
import com.trendizm.backend.auth.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 보안 설정. 로그인은 /api/auth/login에서 직접 확인하고, 이후 요청은 Authorization: Bearer 헤더(JWT)로 인증한다.
 * 이 서버는 브라우저가 아니라 Next 서버(BFF, frontend/app/api)만 호출하므로 CORS 설정이 필요 없다.
 *
 * 접근 규칙(위에서부터 먼저 맞는 규칙이 적용된다):
 * 1. /api/auth/** (가입·로그인·재발급·로그아웃) → 누구나. 단 GET /api/auth/me는 로그인 필요
 * 2. feed·item·lookup·ranking·rating 조회(GET) → 누구나. 쓰기(POST·PUT·PATCH·DELETE)는 로그인 필요
 * 3. /api/admin/** → ADMIN만
 * 4. 그 외(notification, members 등) → 로그인 필요
 * 막히면 SecurityErrorHandler가 401·403을 { code, message }로 응답한다.
 *
 * 보안에 필요한 부품(토큰 확인, 401·403 응답, JWT 설정)은 여기서 직접 등록한다.
 * 그래서 컨트롤러 테스트(@WebMvcTest)는 @Import(SecurityConfig.class) 하나로 실제와 같은 보안 설정을 쓸 수 있다.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
@Import({JwtTokenProvider.class, SecurityErrorHandler.class})
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenProvider tokenProvider;
    private final SecurityErrorHandler securityErrorHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 쿠키로 인증하지 않고 Authorization 헤더로만 인증하므로 CSRF 토큰은 쓰지 않는다
                // (브라우저 쪽 쿠키는 Next 서버가 SameSite=Lax로 저장한다)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtAuthenticationFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(securityErrorHandler)
                        .accessDeniedHandler(securityErrorHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        // permitAll보다 먼저 둔다. 순서가 바뀌면 로그인 없이 열린다
                        .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()
                        .requestMatchers("/api/auth/**").permitAll()
                        // 공개 경로는 조회만 허용한다. 메서드를 빼면 비로그인 쓰기·삭제까지 열린다
                        .requestMatchers(HttpMethod.GET,
                                "/api/feeds/**", "/api/items/**", "/api/lookup/**",
                                "/api/rankings/**", "/api/ratings/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
