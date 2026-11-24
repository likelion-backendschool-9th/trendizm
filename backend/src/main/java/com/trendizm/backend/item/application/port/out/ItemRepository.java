package com.trendizm.backend.item.application.port.out;

import com.trendizm.backend.item.domain.entity.Item;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** 추천 아이템(items) 저장소. 구현체는 adapter/out/persistence/JpaItemRepository (Spring Data가 자동 구현) */
public interface ItemRepository {

    Item save(Item item);

    Optional<Item> findById(Long id);

    List<Item> findByQuestionIdOrderByIdAsc(Long questionId);

    long countByQuestionId(Long questionId);

    /** 질문 id별 추천 아이템 수. 추천이 없는 질문은 결과에 없다 */
    List<CountByKey> countGroupByQuestionId(Collection<Long> questionIds);
}
