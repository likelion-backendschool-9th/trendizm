package com.trendizm.backend.lookup.adapter.out.persistence;

import com.trendizm.backend.lookup.application.port.out.MemeRepository;
import com.trendizm.backend.lookup.domain.Meme;
import org.springframework.data.jpa.repository.JpaRepository;

/** MemeRepository(out 포트)의 메서드를 Spring Data가 구현한다. 서비스는 MemeRepository만 알고 이 인터페이스는 모른다. */
public interface JpaMemeRepository extends JpaRepository<Meme, Long>, MemeRepository {
}
