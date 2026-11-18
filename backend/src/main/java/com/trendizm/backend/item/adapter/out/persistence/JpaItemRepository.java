package com.trendizm.backend.item.adapter.out.persistence;

import com.trendizm.backend.item.application.port.out.CountByKey;
import com.trendizm.backend.item.application.port.out.ItemRepository;
import com.trendizm.backend.item.domain.entity.Item;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** ItemRepository(out 포트)의 메서드를 Spring Data가 구현한다. 서비스는 ItemRepository만 알고 이 인터페이스는 모른다. */
public interface JpaItemRepository extends JpaRepository<Item, Long>, ItemRepository {

    @Override
    @Query("""
            select new com.trendizm.backend.item.application.port.out.CountByKey(i.questionId, count(i))
            from Item i
            where i.questionId in :questionIds
            group by i.questionId
            """)
    List<CountByKey> countGroupByQuestionId(@Param("questionIds") Collection<Long> questionIds);
}
