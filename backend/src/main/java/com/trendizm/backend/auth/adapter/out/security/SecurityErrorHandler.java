package com.trendizm.backend.auth.adapter.out.security;

import com.trendizm.backend.global.error.CommonErrorCode;
import com.trendizm.backend.global.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * 보안 필터에서 막힌 요청도 GlobalExceptionHandler와 같은 { code, message }로 응답한다.
 * 필터에서 난 예외는 @RestControllerAdvice까지 가지 않아서 여기서 따로 처리한다.
 * - 로그인이 필요한데 안 했거나 토큰이 만료됨 → 401 UNAUTHORIZED (프론트는 /api/auth/reissue 후 한 번 다시 요청)
 * - 로그인했지만 권한이 없음 → 403 FORBIDDEN
 * SecurityConfig가 @Import로 등록한다(@Component를 붙이지 않는다).
 */
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        write(response, CommonErrorCode.UNAUTHORIZED);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        write(response, CommonErrorCode.FORBIDDEN);
    }

    /** 메시지는 CommonErrorCode에 고정된 값이라 JSON 특수문자가 없다 */
    private void write(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.status().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(
                "{\"code\":\"" + errorCode.code() + "\",\"message\":\"" + errorCode.message() + "\"}");
    }
}
