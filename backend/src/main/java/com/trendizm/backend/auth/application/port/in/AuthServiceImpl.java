package com.trendizm.backend.auth.application.port.in;

import com.trendizm.backend.auth.application.dto.AvailabilityResponse;
import com.trendizm.backend.auth.application.dto.LoginMember;
import com.trendizm.backend.auth.application.dto.LoginResponse;
import com.trendizm.backend.auth.application.dto.RefreshTokenEntry;
import com.trendizm.backend.auth.application.exception.AuthErrorCode;
import com.trendizm.backend.auth.application.port.AuthService;
import com.trendizm.backend.auth.application.port.out.RefreshTokenStore;
import com.trendizm.backend.auth.application.port.out.TokenProvider;
import com.trendizm.backend.auth.config.JwtProperties;
import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.member.application.dto.MemberCredentials;
import com.trendizm.backend.member.application.dto.MemberInfo;
import com.trendizm.backend.member.application.dto.RegisterMemberCommand;
import com.trendizm.backend.member.application.port.MemberService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 회원 정보는 member 도메인이 저장하고, auth는 MemberService(in 포트)로만 호출한다 */
@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final JwtProperties jwtProperties;

    /** 없는 이메일로 로그인할 때도 비밀번호 비교 시간을 들여, 응답 시간으로 가입 여부를 알 수 없게 한다 */
    private final String dummyPasswordHash;

    public AuthServiceImpl(MemberService memberService, PasswordEncoder passwordEncoder,
            TokenProvider tokenProvider, RefreshTokenStore refreshTokenStore, JwtProperties jwtProperties) {
        this.memberService = memberService;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.refreshTokenStore = refreshTokenStore;
        this.jwtProperties = jwtProperties;
        this.dummyPasswordHash = passwordEncoder.encode("dummy-password-for-timing");
    }

    @Override
    @Transactional
    public LoginResponse signup(String email, String password, String nickname, boolean marketingAgreed) {
        Long memberId = memberService.register(new RegisterMemberCommand(
                email, passwordEncoder.encode(password), nickname, marketingAgreed));
        return issueTokens(memberService.getMember(memberId), false);
    }

    @Override
    public AvailabilityResponse checkEmail(String email) {
        return new AvailabilityResponse(memberService.isEmailAvailable(email));
    }

    @Override
    public AvailabilityResponse checkNickname(String nickname) {
        return new AvailabilityResponse(memberService.isNicknameAvailable(nickname));
    }

    @Override
    public LoginResponse login(String email, String password, boolean rememberMe) {
        MemberCredentials credentials = memberService.findCredentials(email).orElse(null);
        if (credentials == null) {
            passwordEncoder.matches(password, dummyPasswordHash);
            throw new BusinessException(AuthErrorCode.LOGIN_FAILED);
        }
        if (!passwordEncoder.matches(password, credentials.encodedPassword()) || credentials.withdrawn()) {
            throw new BusinessException(AuthErrorCode.LOGIN_FAILED);
        }
        // 비밀번호가 맞을 때만 알려 준다. 틀린 비밀번호로는 이용 제한 여부를 알 수 없다
        if (credentials.suspended()) {
            throw new BusinessException(AuthErrorCode.MEMBER_SUSPENDED);
        }
        return issueTokens(memberService.getMember(credentials.id()), rememberMe);
    }

    @Override
    public LoginResponse reissue(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
        RefreshTokenEntry entry = refreshTokenStore.consume(refreshToken)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN));
        MemberInfo member;
        try {
            member = memberService.getMember(entry.memberId());
        } catch (BusinessException e) {
            throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
        if (member.withdrawn()) {
            throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
        // 로그인 중에 이용이 제한되면 재발급을 막는다. 쓴 리프레시 토큰은 consume에서 이미 지워졌다.
        // 이미 발급된 액세스 토큰은 만료(30분)까지 쓸 수 있다
        if (member.suspended()) {
            throw new BusinessException(AuthErrorCode.MEMBER_SUSPENDED);
        }
        return issueTokens(member, entry.rememberMe());
    }

    private LoginResponse issueTokens(MemberInfo member, boolean rememberMe) {
        String accessToken = tokenProvider.createAccessToken(member.id(), member.role());
        String refreshToken = refreshTokenStore.issue(member.id(), rememberMe);
        return new LoginResponse(
                new LoginMember(member.id(), member.nickname(), member.role()),
                accessToken, jwtProperties.accessTokenTtl().toSeconds(),
                refreshToken, jwtProperties.refreshTokenTtl().toSeconds(),
                rememberMe);
    }
}
