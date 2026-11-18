package com.trendizm.backend.member.application.dto;

import com.trendizm.backend.member.domain.Member;

/**
 * 로그인 확인용 회원 정보. 암호화된 비밀번호가 들어 있으므로 auth(로그인)에서만 쓴다.
 * 응답이나 로그에 그대로 내보내지 않는다.
 */
public record MemberCredentials(Long id, String encodedPassword, String role, boolean withdrawn) {

    public static MemberCredentials from(Member member) {
        return new MemberCredentials(
                member.getId(), member.getPassword(), member.getRole().name(), member.isWithdrawn());
    }

    /** 실수로 로그에 찍혀도 비밀번호가 보이지 않게 한다 */
    @Override
    public String toString() {
        return "MemberCredentials[id=" + id + ", role=" + role + ", withdrawn=" + withdrawn + "]";
    }
}
