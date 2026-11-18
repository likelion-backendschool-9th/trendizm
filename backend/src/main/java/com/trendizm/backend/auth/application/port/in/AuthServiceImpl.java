package com.trendizm.backend.auth.application.port.in;

import com.trendizm.backend.auth.application.dto.AvailabilityResponse;
import com.trendizm.backend.auth.application.port.AuthService;
import com.trendizm.backend.member.application.dto.RegisterMemberCommand;
import com.trendizm.backend.member.application.port.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 회원 정보는 member 도메인이 저장하고, auth는 MemberService(in 포트)로만 호출한다 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Long signup(String email, String password, String nickname, boolean marketingAgreed) {
        // TODO(auth): 3.3에서 가입 직후 로그인 쿠키(액세스·리프레시 토큰)를 발급한다
        return memberService.register(new RegisterMemberCommand(
                email, passwordEncoder.encode(password), nickname, marketingAgreed));
    }

    @Override
    public AvailabilityResponse checkEmail(String email) {
        return new AvailabilityResponse(memberService.isEmailAvailable(email));
    }

    @Override
    public AvailabilityResponse checkNickname(String nickname) {
        return new AvailabilityResponse(memberService.isNicknameAvailable(nickname));
    }
}
