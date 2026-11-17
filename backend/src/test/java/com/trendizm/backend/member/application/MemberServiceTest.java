package com.trendizm.backend.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.member.application.dto.MemberInfo;
import com.trendizm.backend.member.application.exception.MemberErrorCode;
import com.trendizm.backend.member.application.port.MemberService;
import com.trendizm.backend.member.application.port.out.MemberRepository;
import com.trendizm.backend.member.domain.Role;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Sql("/test.sql")
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    private Long idOf(String email) {
        return memberRepository.findByEmail(email).orElseThrow().getId();
    }

    @Test
    @DisplayName("id로 회원 정보를 가져온다")
    void 회원_조회() {
        MemberInfo info = memberService.getMember(idOf("member@test.com"));

        assertThat(info.nickname()).isEqualTo("테스트회원");
        assertThat(info.role()).isEqualTo(Role.USER);
        assertThat(info.withdrawn()).isFalse();
    }

    @Test
    @DisplayName("탈퇴 회원도 조회되고 withdrawn이 true다")
    void 탈퇴_회원_조회() {
        MemberInfo info = memberService.getMember(idOf("withdrawn@test.com"));

        assertThat(info.withdrawn()).isTrue();
    }

    @Test
    @DisplayName("없는 회원이면 MEMBER_NOT_FOUND 예외를 던진다")
    void 없는_회원() {
        assertThatThrownBy(() -> memberService.getMember(-1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("여러 id로 한 번에 가져오고, 없는 id는 빠진다")
    void 여러_회원_조회() {
        List<MemberInfo> infos = memberService.getMembers(
                List.of(idOf("member@test.com"), idOf("withdrawn@test.com"), -1L));

        assertThat(infos).extracting(MemberInfo::nickname)
                .containsExactlyInAnyOrder("테스트회원", "탈퇴회원");
    }

    @Test
    @DisplayName("빈 목록을 넘기면 빈 목록을 돌려준다")
    void 빈_목록() {
        assertThat(memberService.getMembers(List.of())).isEmpty();
    }
}
