package com.trendizm.backend.point.adapter.out.persistence;

import com.trendizm.backend.point.application.port.out.PointHistoryRepository;
import com.trendizm.backend.point.domain.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

/** PointHistoryRepository(out 포트)의 메서드를 Spring Data가 구현한다. 서비스는 PointHistoryRepository만 알고 이 인터페이스는 모른다. */
public interface JpaPointHistoryRepository extends JpaRepository<PointHistory, Long>, PointHistoryRepository {
}
