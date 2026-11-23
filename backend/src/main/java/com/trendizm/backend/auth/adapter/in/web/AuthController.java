package com.trendizm.backend.auth.adapter.in.web;

import com.trendizm.backend.auth.adapter.in.web.dto.LoginRequest;
import com.trendizm.backend.auth.adapter.in.web.dto.LogoutRequest;
import com.trendizm.backend.auth.adapter.in.web.dto.ReissueRequest;
import com.trendizm.backend.auth.adapter.in.web.dto.SignupRequest;
import com.trendizm.backend.auth.application.dto.AvailabilityResponse;
import com.trendizm.backend.auth.application.dto.LoginMember;
import com.trendizm.backend.auth.application.dto.LoginResponse;
import com.trendizm.backend.auth.application.port.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 로그인 없이 모든 요청 허용 (auth/adapter/out/security/SecurityConfig의 /api/auth/**). 단 GET /api/auth/me는 로그인 필요.
 * 이 API는 브라우저가 아니라 Next 서버(BFF, frontend/app/api)가 호출한다.
 * 토큰은 응답 본문(LoginResponse)으로 주고, 쿠키 저장은 Next 서버가 한다.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 가입 후 바로 로그인 상태가 된다 */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(
                request.email(), request.password(), request.nickname(), request.marketingAgreed());
    }

    @GetMapping("/check-email")
    public AvailabilityResponse checkEmail(
            @RequestParam @NotBlank(message = "이메일을 입력해 주세요.") @Email(message = "이메일 형식이 아닙니다.")
            String email) {
        return authService.checkEmail(email);
    }

    @GetMapping("/check-nickname")
    public AvailabilityResponse checkNickname(
            @RequestParam @NotBlank(message = "닉네임을 입력해 주세요.") String nickname) {
        return authService.checkNickname(nickname);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.email(), request.password(), request.rememberMe());
    }

    /** 액세스 토큰이 만료되면 Next 서버가 refresh 토큰을 본문에 담아 호출한다. 본문이 없거나 비어 있으면 401 */
    @PostMapping("/reissue")
    public LoginResponse reissue(@RequestBody(required = false) ReissueRequest request) {
        return authService.reissue(request == null ? null : request.refreshToken());
    }

    /** Next 서버가 refresh_token 쿠키를 본문에 담아 호출한다. 이 기기의 리프레시 토큰만 폐기하고, 쿠키 삭제는 Next 서버가 한다 */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestBody(required = false) LogoutRequest request) {
        authService.logout(request == null ? null : request.refreshToken());
    }

    /** 헤더에 보일 로그인 회원 정보 */
    @GetMapping("/me")
    public LoginMember me(@AuthenticationPrincipal(expression = "id") Long userId) {
        return authService.me(userId);
    }
}
