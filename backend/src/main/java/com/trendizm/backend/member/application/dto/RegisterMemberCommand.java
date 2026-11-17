package com.trendizm.backend.member.application.dto;

/**
 * 회원 등록에 필요한 값. 회원가입(auth)이 MemberService.register로 넘긴다.
 * encodedPassword는 PasswordEncoder로 암호화한 값이어야 한다.
 */
public record RegisterMemberCommand(String email, String encodedPassword, String nickname, boolean marketingAgreed) {
}
