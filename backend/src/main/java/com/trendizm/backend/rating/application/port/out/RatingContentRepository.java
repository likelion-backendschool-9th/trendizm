package com.trendizm.backend.rating.application.port.out;

import com.trendizm.backend.rating.domain.entity.RatingContent;
import com.trendizm.backend.rating.domain.enums.RatingContentCategory;
import com.trendizm.backend.rating.domain.enums.RatingContentStatus;
import com.trendizm.backend.rating.domain.enums.RatingContentSubcategory;
import com.trendizm.backend.rating.domain.enums.RatingContentVisibility;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 평점 콘텐츠(rating_contents) 저장소. 구현체는 adapter/out/persistence/JpaRatingContentRepository.
 * 목록 필터는 (전체 | 대분류 | 소분류) × (진행 상태 있음 | 없음) 조합마다 메서드를 둔다.
 * (null이면 조건을 빼는 JPQL은 PostgreSQL이 null 파라미터의 타입을 정하지 못해 실패할 수 있다)
 */
public interface RatingContentRepository {

    RatingContent save(RatingContent content);

    Optional<RatingContent> findById(Long id);

    Page<RatingContent> findByVisibility(RatingContentVisibility visibility, Pageable pageable);

    Page<RatingContent> findByVisibilityAndStatus(RatingContentVisibility visibility, RatingContentStatus status,
                                                  Pageable pageable);

    Page<RatingContent> findByVisibilityAndCategory(RatingContentVisibility visibility, RatingContentCategory category,
                                                    Pageable pageable);

    Page<RatingContent> findByVisibilityAndCategoryAndStatus(RatingContentVisibility visibility,
                                                             RatingContentCategory category,
                                                             RatingContentStatus status, Pageable pageable);

    Page<RatingContent> findByVisibilityAndSubcategory(RatingContentVisibility visibility,
                                                       RatingContentSubcategory subcategory, Pageable pageable);

    Page<RatingContent> findByVisibilityAndSubcategoryAndStatus(RatingContentVisibility visibility,
                                                                RatingContentSubcategory subcategory,
                                                                RatingContentStatus status, Pageable pageable);
}
