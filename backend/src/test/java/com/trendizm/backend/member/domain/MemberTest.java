package com.trendizm.backend.member.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberTest {

    @Test
    @DisplayName("가입하면 USER 권한, 알림 수신, ACTIVE 상태, 활동 중으로 만들어진다")
    void 회원_생성() {
        Member member = Member.create("a@test.com", "encoded", "닉네임", true);

        assertThat(member.getEmail()).isEqualTo("a@test.com");
        assertThat(member.getNickname()).isEqualTo("닉네임");
        assertThat(member.getRole()).isEqualTo(Role.USER);
        assertThat(member.isNotify()).isTrue();
        assertThat(member.isMarketingAgreed()).isTrue();
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(member.isWithdrawn()).isFalse();
    }

    @Test
    @DisplayName("비밀번호를 바꾸면 새 값으로 바뀐다")
    void 비밀번호_변경() {
        Member member = Member.create("a@test.com", "old", "닉네임", false);

        member.changePassword("new");

        assertThat(member.getPassword()).isEqualTo("new");
    }

    @Test
    @DisplayName("탈퇴하면 탈퇴 시각이 기록된다")
    void 탈퇴() {
        Member member = Member.create("a@test.com", "encoded", "닉네임", false);

        member.withdraw();

        assertThat(member.isWithdrawn()).isTrue();
        assertThat(member.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 탈퇴한 회원이 다시 탈퇴해도 처음 탈퇴 시각이 유지된다")
    void 중복_탈퇴() {
        Member member = Member.create("a@test.com", "encoded", "닉네임", false);
        member.withdraw();
        LocalDateTime first = member.getDeletedAt();

        member.withdraw();

        assertThat(member.getDeletedAt()).isEqualTo(first);
    }
}
