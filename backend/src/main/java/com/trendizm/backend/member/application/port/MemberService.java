package com.trendizm.backend.member.application.port;

import com.trendizm.backend.member.application.dto.MemberCredentials;
import com.trendizm.backend.member.application.dto.MemberInfo;
import com.trendizm.backend.member.application.dto.RegisterMemberCommand;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * 회원 유스케이스 (in 포트). 다른 도메인은 회원 정보를 이 인터페이스로만 가져간다.
 * 로그인(auth), 마이페이지 기능은 각 작업에서 메서드를 추가한다.
 */
public interface MemberService {

    /**
     * 알림을 받을 회원인지. 알림 수신을 켰고(users.notify) 탈퇴·이용 제한 상태가 아니면 true.
     * 없는 회원이면 예외 대신 false (알림은 만들지 않으면 그만이라서)
     */
    boolean isNotificationEnabled(Long memberId);

    /** 탈퇴 회원도 반환한다(withdrawn = true). 없으면 MEMBER_NOT_FOUND */
    MemberInfo getMember(Long memberId);

    /**
     * 목록에 작성자 닉네임을 붙일 때 쓴다. 회원 id → 회원 정보. 없는 id는 결과에서 빠진다.
     * 예: Map<Long, MemberInfo> members = memberService.getMembers(userIds);
     *     members.get(comment.getUserId())
     */
    Map<Long, MemberInfo> getMembers(Collection<Long> memberIds);

    /**
     * 회원을 등록하고 id를 돌려준다.
     * 이메일이 이미 있으면 DUPLICATE_EMAIL, 닉네임이 이미 있으면 DUPLICATE_NICKNAME,
     * 동시에 같은 값으로 가입해 DB에서 막히면 DUPLICATE_MEMBER
     */
    Long register(RegisterMemberCommand command);

    /** 가입에 쓸 수 있는 이메일인지. 대소문자·앞뒤 공백은 무시한다 */
    boolean isEmailAvailable(String email);

    /** 가입에 쓸 수 있는 닉네임인지. 앞뒤 공백은 무시한다 */
    boolean isNicknameAvailable(String nickname);

    /**
     * 로그인 확인용. auth(로그인)에서만 호출한다. 대소문자·앞뒤 공백은 무시하고 찾는다.
     * 탈퇴 회원도 반환하므로(withdrawn = true) 호출하는 쪽에서 거른다.
     */
    Optional<MemberCredentials> findCredentials(String email);
}
