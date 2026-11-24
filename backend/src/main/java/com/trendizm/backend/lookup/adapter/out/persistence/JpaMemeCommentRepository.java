package com.trendizm.backend.lookup.adapter.out.persistence;

import com.trendizm.backend.lookup.application.port.out.MemeCommentRepository;
import com.trendizm.backend.lookup.domain.entity.MemeComment;
import org.springframework.data.jpa.repository.JpaRepository;

/** MemeCommentRepository(out 포트)의 메서드를 Spring Data가 구현한다 */
public interface JpaMemeCommentRepository extends JpaRepository<MemeComment, Long>, MemeCommentRepository {
}
