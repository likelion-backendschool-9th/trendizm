package com.trendizm.backend.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.member.application.dto.MemberInfo;
import com.trendizm.backend.member.application.exception.MemberErrorCode;
import com.trendizm.backend.member.application.port.MemberService;
import com.trendizm.backend.member.application.port.out.MemberRepository;
import com.trendizm.backend.member.domain.Member;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    /** 활동 중인 회원 1명, 탈퇴한 회원 1명. @Transactional이라 테스트가 끝나면 롤백된다 */
    @BeforeEach
    void setUp() {
        memberRepository.save(Member.create("member@test.com", "encoded-password", "테스트회원", false));
        Member withdrawn = Member.create("withdrawn@test.com", "encoded-password", "탈퇴회원", false);
        withdrawn.withdraw();
        memberRepository.save(withdrawn);
    }

    private Long idOf(String email) {
        return memberRepository.findByEmail(email).orElseThrow().getId();
    }

    @Test
    @DisplayName("id로 회원 정보를 가져온다")
    void 회원_조회() {
        MemberInfo info = memberService.getMember(idOf("member@test.com"));

        assertThat(info.nickname()).isEqualTo("테스트회원");
        assertThat(info.role()).isEqualTo("USER");
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
    @DisplayName("여러 id로 한 번에 가져와 id로 꺼낼 수 있고, 없는 id는 빠진다")
    void 여러_회원_조회() {
        Long memberId = idOf("member@test.com");
        Long withdrawnId = idOf("withdrawn@test.com");

        Map<Long, MemberInfo> infos = memberService.getMembers(List.of(memberId, withdrawnId, -1L));

        assertThat(infos).hasSize(2).doesNotContainKey(-1L);
        assertThat(infos.get(memberId).nickname()).isEqualTo("테스트회원");
        assertThat(infos.get(withdrawnId).withdrawn()).isTrue();
    }

    @Test
    @DisplayName("빈 목록을 넘기면 빈 목록을 돌려준다")
    void 빈_목록() {
        assertThat(memberService.getMembers(List.of())).isEmpty();
    }
}
