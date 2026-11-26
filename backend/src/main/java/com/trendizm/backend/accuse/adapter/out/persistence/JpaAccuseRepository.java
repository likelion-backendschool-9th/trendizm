package com.trendizm.backend.accuse.adapter.out.persistence;

import com.trendizm.backend.accuse.application.port.out.AccuseRepository;
import com.trendizm.backend.accuse.domain.entity.Accuse;
import org.springframework.data.jpa.repository.JpaRepository;

/** AccuseRepository(out 포트)의 메서드를 Spring Data가 구현한다 */
public interface JpaAccuseRepository extends JpaRepository<Accuse, Long>, AccuseRepository {
}
