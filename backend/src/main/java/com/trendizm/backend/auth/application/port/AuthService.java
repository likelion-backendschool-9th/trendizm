package com.trendizm.backend.auth.application.port;

import com.trendizm.backend.auth.application.dto.AvailabilityResponse;
import com.trendizm.backend.auth.application.dto.LoginMember;
import com.trendizm.backend.auth.application.dto.LoginResponse;

/** 인증 유스케이스. 로그아웃·비밀번호 찾기 등은 각 작업에서 메서드를 추가한다. */
public interface AuthService {

    /**
     * 회원가입 후 바로 로그인 상태로 만든다(로그인 상태 유지는 꺼진 상태).
     * 이메일·닉네임이 이미 있으면 409 (MemberErrorCode.DUPLICATE_*)
     */
    LoginResponse signup(String email, String password, String nickname, boolean marketingAgreed);

    AvailabilityResponse checkEmail(String email);

    AvailabilityResponse checkNickname(String nickname);

    /** 이메일이 없거나, 비밀번호가 틀리거나, 탈퇴한 회원이면 모두 LOGIN_FAILED(401) */
    LoginResponse login(String email, String password, boolean rememberMe);

    /** 리프레시 토큰으로 두 토큰을 새로 발급한다. 쓴 리프레시 토큰은 다시 쓸 수 없다. 실패하면 INVALID_REFRESH_TOKEN(401) */
    LoginResponse reissue(String refreshToken);

    /** 이 기기의 리프레시 토큰을 폐기한다. 토큰이 없거나 이미 없는 토큰이어도 성공으로 본다 */
    void logout(String refreshToken);

    /**
     * 로그인한 회원 정보(헤더 표시용). 토큰은 맞지만 회원이 없거나 탈퇴했으면 UNAUTHORIZED(401),
     * 이용 제한이면 MEMBER_SUSPENDED(403)
     */
    LoginMember me(Long memberId);
}
