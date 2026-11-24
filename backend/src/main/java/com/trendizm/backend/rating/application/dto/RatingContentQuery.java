package com.trendizm.backend.rating.application.dto;

import com.trendizm.backend.rating.application.port.RatingContentSort;
import com.trendizm.backend.rating.domain.enums.RatingContentCategory;
import com.trendizm.backend.rating.domain.enums.RatingContentStatus;
import com.trendizm.backend.rating.domain.enums.RatingContentSubcategory;

/**
 * 콘텐츠 목록 조건. category·subcategory·status는 비어 있으면 전체다.
 * category와 subcategory가 둘 다 있으면 소분류가 그 대분류에 속해야 한다(아니면 RATING_CATEGORY_MISMATCH).
 * sort가 비어 있으면 최신 등록순이다.
 */
public record RatingContentQuery(RatingContentCategory category, RatingContentSubcategory subcategory,
                                 RatingContentStatus status, RatingContentSort sort, int page, int size) {

    public RatingContentQuery {
        sort = sort == null ? RatingContentSort.LATEST : sort;
    }

    /** 최신 등록순 */
    public RatingContentQuery(RatingContentCategory category, RatingContentSubcategory subcategory,
                              RatingContentStatus status, int page, int size) {
        this(category, subcategory, status, RatingContentSort.LATEST, page, size);
    }
}
