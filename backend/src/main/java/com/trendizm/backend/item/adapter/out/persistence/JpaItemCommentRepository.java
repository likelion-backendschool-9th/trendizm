package com.trendizm.backend.item.adapter.out.persistence;

import com.trendizm.backend.item.application.port.out.CountByKey;
import com.trendizm.backend.item.application.port.out.ItemCommentRepository;
import com.trendizm.backend.item.domain.entity.ItemComment;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** ItemCommentRepository(out 포트)의 메서드를 Spring Data가 구현한다 */
public interface JpaItemCommentRepository extends JpaRepository<ItemComment, Long>, ItemCommentRepository {

    @Override
    @Query("""
            select new com.trendizm.backend.item.application.port.out.CountByKey(c.questionId, count(c))
            from ItemComment c
            where c.questionId in :questionIds
            group by c.questionId
            """)
    List<CountByKey> countGroupByQuestionId(@Param("questionIds") Collection<Long> questionIds);
}
