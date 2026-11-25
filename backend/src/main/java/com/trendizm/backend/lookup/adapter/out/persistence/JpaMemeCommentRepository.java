package com.trendizm.backend.lookup.adapter.out.persistence;

import com.trendizm.backend.lookup.application.port.out.MemeCommentRepository;
import com.trendizm.backend.lookup.domain.entity.MemeComment;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** MemeCommentRepository(out 포트)의 메서드를 Spring Data가 구현한다. 쿼리·잠금 설정은 out 포트에 JPA를 노출하지 않으려고 여기에 둔다. */
public interface JpaMemeCommentRepository extends JpaRepository<MemeComment, Long>, MemeCommentRepository {

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from MemeComment c where c.id = :id")
    Optional<MemeComment> findByIdForUpdate(@Param("id") Long id);

    @Override
    @Query("""
            select c from MemeComment c
            where c.interpretationId = :interpretationId and c.parentId is null
              and (c.deletedAt is null
                   or exists (select 1 from MemeComment r where r.parentId = c.id and r.deletedAt is null))
            """)
    Page<MemeComment> findVisibleComments(@Param("interpretationId") Long interpretationId, Pageable pageable);
}
