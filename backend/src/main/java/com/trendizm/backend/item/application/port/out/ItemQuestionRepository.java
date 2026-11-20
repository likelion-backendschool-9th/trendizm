package com.trendizm.backend.item.application.port.out;

import com.trendizm.backend.item.domain.entity.ItemQuestion;
import com.trendizm.backend.item.domain.enums.ItemCategory;
import com.trendizm.backend.item.domain.enums.ItemQuestionStatus;
import com.trendizm.backend.item.domain.enums.ItemSubcategory;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** 구현체는 adapter/out/persistence/JpaItemQuestionRepository (Spring Data가 자동 구현) */
public interface ItemQuestionRepository {

    ItemQuestion save(ItemQuestion question);

    Optional<ItemQuestion> findById(Long id);

    Page<ItemQuestion> findByStatus(ItemQuestionStatus status, Pageable pageable);

    Page<ItemQuestion> findByStatusAndCategory(ItemQuestionStatus status, ItemCategory category, Pageable pageable);

    Page<ItemQuestion> findByStatusAndSubcategory(ItemQuestionStatus status, ItemSubcategory subcategory,
                                                  Pageable pageable);
}
