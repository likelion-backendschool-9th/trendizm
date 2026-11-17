package com.trendizm.backend.member.domain;

import com.trendizm.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * users 테이블. 스프링 시큐리티의 User 클래스와 헷갈리지 않게 클래스 이름은 Member로 한다.
 * 탈퇴는 소프트 삭제다: deletedAt이 비어 있으면 활동 중, 값이 있으면 탈퇴 시각.
 * 탈퇴 30일 뒤 이메일·닉네임 익명화는 3.10에서 추가한다.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /** 암호화한 값만 저장한다. 평문 비밀번호를 넣지 않는다 */
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 30)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /** 서비스 알림 수신 여부 */
    @Column(nullable = false)
    private boolean notify;

    /** 마케팅 정보 수신 동의 */
    @Column(nullable = false)
    private boolean marketingAgreed;

    private LocalDateTime deletedAt;

    private Member(String email, String password, String nickname, boolean marketingAgreed) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = Role.USER;
        this.notify = true;
        this.marketingAgreed = marketingAgreed;
    }

    /**
     * 회원가입. encodedPassword는 PasswordEncoder로 암호화한 값이어야 한다.
     * 이메일은 normalizeEmail로 바꿔 저장하고, 닉네임은 앞뒤 공백을 지운다.
     */
    public static Member create(String email, String encodedPassword, String nickname, boolean marketingAgreed) {
        return new Member(normalizeEmail(email), encodedPassword, nickname.strip(), marketingAgreed);
    }

    /**
     * 이메일 비교용 표준 형태: 앞뒤 공백 제거 + 소문자.
     * A@Test.com과 a@test.com을 같은 회원으로 본다. 이메일로 조회할 때도 이 값으로 찾는다.
     */
    public static String normalizeEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    /** encodedPassword는 PasswordEncoder로 암호화한 값이어야 한다 */
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    /** 탈퇴. 이미 탈퇴했으면 처음 탈퇴 시각을 그대로 둔다 */
    public void withdraw() {
        if (deletedAt == null) {
            deletedAt = LocalDateTime.now();
        }
    }

    public boolean isWithdrawn() {
        return deletedAt != null;
    }
}
