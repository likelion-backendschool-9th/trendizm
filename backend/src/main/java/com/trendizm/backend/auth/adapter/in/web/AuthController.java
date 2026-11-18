package com.trendizm.backend.auth.adapter.in.web;

import com.trendizm.backend.auth.adapter.in.web.dto.SignupRequest;
import com.trendizm.backend.auth.application.dto.AvailabilityResponse;
import com.trendizm.backend.auth.application.port.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 로그인 없이 모든 요청 허용 (auth/adapter/out/security/SecurityConfig의 /api/auth/**) */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public void signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request.email(), request.password(), request.nickname(), request.marketingAgreed());
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
}
