package com.trendizm.backend.lookup.application.port.out;

import com.trendizm.backend.lookup.domain.entity.MemeComment;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** 구현체는 adapter/out/persistence/JpaMemeCommentRepository (Spring Data가 자동 구현) */
public interface MemeCommentRepository {

    MemeComment save(MemeComment comment);

    Optional<MemeComment> findById(Long id);

    void delete(MemeComment comment);

    /** 변경 감지한 수정을 바로 DB에 반영한다. 응답에 새 updatedAt을 담을 때 쓴다 */
    void flush();

    /** 댓글(대댓글 제외) 목록 */
    Page<MemeComment> findByInterpretationIdAndParentIdIsNull(Long interpretationId, Pageable pageable);

    /** 여러 댓글의 대댓글을 오래된 순으로 */
    List<MemeComment> findByParentIdInOrderByIdAsc(Collection<Long> parentIds);

    /** 이 댓글에 대댓글이 남아 있는지 */
    boolean existsByParentId(Long parentId);
}
