package com.trendizm.backend.rating.application.dto;

import java.time.LocalDate;
import java.util.Map;

/**
 * 관리자 콘텐츠 등록. 관리자 API(POST /api/admin/ratings)가 요청을 검사한 뒤 넘긴다.
 * 관리자 API는 평점 도메인 밖에 있어서 평점 enum을 쓸 수 없으므로, 소분류·진행 상태는 slug 문자열로 받는다.
 * 예: subcategory = "drama", status = "ongoing". 대분류는 소분류에서 정해진다. subcategory·status·title만 필수다.
 * extra의 키는 소분류마다 정해져 있다. 예: 드라마 { "season": 2, "episodeLength": "60분" } (RatingContentExtraKey 참고)
 */
public record RegisterRatingContentCommand(
        String subcategory,
        String status,
        String title,
        String description,
        String imageUrl,
        Integer ageRating,
        LocalDate releasedAt,
        Map<String, Object> extra
) {
}
