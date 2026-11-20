package com.trendizm.backend.item.adapter.out.persistence;

import com.trendizm.backend.item.application.port.out.ItemQuestionRepository;
import com.trendizm.backend.item.domain.entity.ItemQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

/** ItemQuestionRepository(out 포트)의 메서드를 Spring Data가 구현한다 */
public interface JpaItemQuestionRepository extends JpaRepository<ItemQuestion, Long>, ItemQuestionRepository {
}
