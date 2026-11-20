package com.trendizm.backend.rating.application.dto;

import com.trendizm.backend.rating.domain.enums.RatingContentCategory;
import com.trendizm.backend.rating.domain.enums.RatingContentStatus;
import com.trendizm.backend.rating.domain.enums.RatingContentSubcategory;

/**
 * 콘텐츠 목록 조건. category·subcategory·status는 비어 있으면 전체다.
 * category와 subcategory가 둘 다 있으면 소분류가 그 대분류에 속해야 한다(아니면 RATING_CATEGORY_MISMATCH).
 */
public record RatingContentQuery(RatingContentCategory category, RatingContentSubcategory subcategory,
                                 RatingContentStatus status, int page, int size) {
}
