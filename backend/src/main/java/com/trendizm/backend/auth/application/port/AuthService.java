package com.trendizm.backend.auth.application.port;

import com.trendizm.backend.auth.application.dto.AvailabilityResponse;

/** 인증 유스케이스. 로그인·로그아웃 등은 각 작업에서 메서드를 추가한다. */
public interface AuthService {

    /**
     * 회원가입. 비밀번호를 암호화해 회원을 등록하고 회원 id를 돌려준다.
     * 이메일·닉네임이 이미 있으면 409 (MemberErrorCode.DUPLICATE_*)
     */
    Long signup(String email, String password, String nickname, boolean marketingAgreed);

    AvailabilityResponse checkEmail(String email);

    AvailabilityResponse checkNickname(String nickname);
}
