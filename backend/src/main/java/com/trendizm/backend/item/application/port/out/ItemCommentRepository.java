package com.trendizm.backend.item.application.port.out;

import com.trendizm.backend.item.domain.entity.ItemComment;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** 구현체는 adapter/out/persistence/JpaItemCommentRepository (Spring Data가 자동 구현) */
public interface ItemCommentRepository {

    ItemComment save(ItemComment comment);

    Optional<ItemComment> findById(Long id);

    /** 댓글(대댓글 제외) 목록 */
    Page<ItemComment> findByQuestionIdAndParentIdIsNull(Long questionId, Pageable pageable);

    /** 댓글(대댓글 제외) 목록, 공감 많은 순. 같으면 최신 댓글 먼저. pageable의 정렬은 쓰지 않는다 */
    Page<ItemComment> findByQuestionIdAndParentIdIsNullOrderByLikes(Long questionId, Pageable pageable);

    /** 여러 댓글의 대댓글을 오래된 순으로 */
    List<ItemComment> findByParentIdInOrderByIdAsc(Collection<Long> parentIds);

    /** 대댓글 포함 댓글 수 */
    long countByQuestionId(Long questionId);

    /** 질문 id별 댓글 수 (대댓글 포함). 댓글이 없는 질문은 결과에 없다 */
    List<CountByKey> countGroupByQuestionId(Collection<Long> questionIds);
}
