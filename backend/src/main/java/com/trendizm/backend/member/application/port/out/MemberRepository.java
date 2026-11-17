package com.trendizm.backend.member.application.port.out;

import com.trendizm.backend.member.domain.Member;
import java.util.List;
import java.util.Optional;

/**
 * 구현체는 adapter/out/persistence/JpaMemberRepository (Spring Data가 자동 구현).
 * 탈퇴 회원도 조회된다. 활동 중인 회원만 필요하면 member.isWithdrawn()으로 거른다.
 */
public interface MemberRepository {

    Member save(Member member);

    Optional<Member> findById(Long id);

    List<Member> findAllById(Iterable<Long> ids);

    Optional<Member> findByEmail(String email);

    /** 탈퇴 회원 포함. 익명화 전(탈퇴 30일 이내)에는 같은 이메일로 다시 가입할 수 없다 */
    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
}
