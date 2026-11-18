package com.trendizm.backend.member.application.port;

import com.trendizm.backend.member.application.dto.MemberInfo;
import java.util.Collection;
import java.util.Map;

/**
 * 회원 유스케이스 (in 포트). 다른 도메인은 회원 정보를 이 인터페이스로만 가져간다.
 * 회원가입·로그인(auth), 마이페이지 기능은 각 작업에서 메서드를 추가한다.
 */
public interface MemberService {

    /** 탈퇴 회원도 반환한다(withdrawn = true). 없으면 MEMBER_NOT_FOUND */
    MemberInfo getMember(Long memberId);

    /**
     * 목록에 작성자 닉네임을 붙일 때 쓴다. 회원 id → 회원 정보. 없는 id는 결과에서 빠진다.
     * 예: Map<Long, MemberInfo> members = memberService.getMembers(userIds);
     *     members.get(comment.getUserId())
     */
    Map<Long, MemberInfo> getMembers(Collection<Long> memberIds);
}
