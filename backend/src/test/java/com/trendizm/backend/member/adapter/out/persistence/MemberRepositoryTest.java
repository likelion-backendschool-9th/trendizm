package com.trendizm.backend.member.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.trendizm.backend.member.application.port.out.MemberRepository;
import com.trendizm.backend.member.domain.entity.Member;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager entityManager;

    /** 활동 중인 회원 1명, 탈퇴한 회원 1명. @Transactional이라 테스트가 끝나면 롤백된다 */
    @BeforeEach
    void setUp() {
        memberRepository.save(Member.create("member@test.com", "encoded-password", "테스트회원", false));
        Member withdrawn = Member.create("withdrawn@test.com", "encoded-password", "탈퇴회원", false);
        withdrawn.withdraw();
        memberRepository.save(withdrawn);
    }

    @Test
    @DisplayName("저장하면 id와 생성·수정 시각이 채워진다")
    void 저장() {
        Member saved = memberRepository.save(Member.create("new@test.com", "encoded", "새회원", false));
        entityManager.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("이메일로 회원을 찾는다")
    void 이메일로_조회() {
        assertThat(memberRepository.findByEmail("member@test.com"))
                .get()
                .extracting(Member::getNickname)
                .isEqualTo("테스트회원");
        assertThat(memberRepository.findByEmail("none@test.com")).isEmpty();
    }

    @Test
    @DisplayName("이메일·닉네임 중복을 확인한다. 탈퇴 회원도 포함한다")
    void 중복_확인() {
        assertThat(memberRepository.existsByEmail("member@test.com")).isTrue();
        assertThat(memberRepository.existsByEmail("withdrawn@test.com")).isTrue();
        assertThat(memberRepository.existsByEmail("none@test.com")).isFalse();

        assertThat(memberRepository.existsByNickname("테스트회원")).isTrue();
        assertThat(memberRepository.existsByNickname("없는닉네임")).isFalse();
    }
}
