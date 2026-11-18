package com.trendizm.backend.member.application.dto;

import com.trendizm.backend.member.domain.entity.Member;

/**
 * 다른 도메인에 넘기는 회원 정보. 비밀번호·이메일은 담지 않는다.
 * 예: 댓글 목록에 작성자 닉네임을 보여 줄 때, withdrawn이면 "탈퇴한 회원"으로 표시한다.
 * suspended는 관리자가 이용을 제한한 회원이다.
 * role은 "USER", "ADMIN" 문자열이다. 다른 도메인이 member.domain.enums.Role을 import하지 않게 enum 대신 문자열로 넘긴다.
 */
public record MemberInfo(Long id, String nickname, String role, boolean withdrawn, boolean suspended) {

    public static MemberInfo from(Member member) {
        return new MemberInfo(member.getId(), member.getNickname(), member.getRole().name(),
                member.isWithdrawn(), member.isSuspended());
    }
}
