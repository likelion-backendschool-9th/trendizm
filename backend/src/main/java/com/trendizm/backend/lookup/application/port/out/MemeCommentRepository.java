package com.trendizm.backend.lookup.application.port.out;

import com.trendizm.backend.lookup.domain.entity.MemeComment;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 구현체는 adapter/out/persistence/JpaMemeCommentRepository (Spring Data가 자동 구현).
 * 댓글은 지워도 행을 남긴다(deleted_at). 목록 조회는 삭제된 댓글을 걸러서 준다.
 */
public interface MemeCommentRepository {

    MemeComment save(MemeComment comment);

    Optional<MemeComment> findById(Long id);

    /**
     * 댓글 하나를 행 잠금(SELECT ... FOR UPDATE)으로 읽는다. 트랜잭션이 끝날 때까지 다른 수정·삭제·좋아요·답글 작성이 기다린다.
     * 수정과 삭제가 겹쳐 지운 댓글이 되살아나거나, 삭제와 좋아요가 겹쳐 지운 댓글에 좋아요가 남는 것을 막는다.
     */
    Optional<MemeComment> findByIdForUpdate(Long id);

    /** 변경 감지한 수정을 바로 DB에 반영한다. 응답에 새 updatedAt을 담을 때 쓴다 */
    void flush();

    /**
     * 의견의 댓글(대댓글 제외) 목록. 삭제된 댓글은 살아 있는 대댓글이 있을 때만 넣는다("삭제된 댓글입니다"로 표시).
     */
    Page<MemeComment> findVisibleComments(Long interpretationId, Pageable pageable);

    /** 여러 댓글의 삭제되지 않은 대댓글을 오래된 순으로 */
    List<MemeComment> findByParentIdInAndDeletedAtIsNullOrderByIdAsc(Collection<Long> parentIds);
}
