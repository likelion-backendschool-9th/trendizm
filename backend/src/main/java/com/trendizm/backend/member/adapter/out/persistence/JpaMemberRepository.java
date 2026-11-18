package com.trendizm.backend.member.adapter.out.persistence;

import com.trendizm.backend.member.application.port.out.MemberRepository;
import com.trendizm.backend.member.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

/** MemberRepository(out 포트)의 메서드를 Spring Data가 구현한다. 서비스는 MemberRepository만 알고 이 인터페이스는 모른다. */
public interface JpaMemberRepository extends JpaRepository<Member, Long>, MemberRepository {
}
