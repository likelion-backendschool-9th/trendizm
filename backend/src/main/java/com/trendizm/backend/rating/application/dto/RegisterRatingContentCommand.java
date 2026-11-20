package com.trendizm.backend.rating.application.dto;

import com.trendizm.backend.rating.domain.enums.RatingContentStatus;
import com.trendizm.backend.rating.domain.enums.RatingContentSubcategory;
import java.time.LocalDate;
import java.util.Map;

/**
 * 관리자 콘텐츠 등록. 관리자 API(POST /api/admin/ratings)가 요청을 검사한 뒤 넘긴다.
 * 대분류는 소분류에서 정해진다. subcategory·status·title만 필수다.
 */
public record RegisterRatingContentCommand(
        RatingContentSubcategory subcategory,
        RatingContentStatus status,
        String title,
        String description,
        String imageUrl,
        Integer ageRating,
        LocalDate releasedAt,
        Map<String, Object> extra
) {
}
