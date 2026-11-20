package com.trendizm.backend.rating.domain.enums;

import java.util.Map;

/**
 * 콘텐츠 진행 상태 (ERD rating_contents.status, varchar(30)). 관리자 숨김(visibility)과는 별개다.
 * 화면 이름은 대분류마다 다르다. 예: 미디어 ONGOING = "방영중", 게임 ONGOING = "서비스중"
 */
public enum RatingContentStatus {

    UPCOMING(Map.of(
            RatingContentCategory.MEDIA, "공개 예정",
            RatingContentCategory.FOOD, "출시 예정",
            RatingContentCategory.MUSIC, "발매·공연 예정",
            RatingContentCategory.BOOKS, "연재 예정",
            RatingContentCategory.GAME, "출시 예정",
            RatingContentCategory.PLACE, "오픈 예정")),
    ONGOING(Map.of(
            RatingContentCategory.MEDIA, "방영중",
            RatingContentCategory.FOOD, "판매중",
            RatingContentCategory.MUSIC, "발매·공연중",
            RatingContentCategory.BOOKS, "연재중",
            RatingContentCategory.GAME, "서비스중",
            RatingContentCategory.PLACE, "진행중")),
    ENDED(Map.of(
            RatingContentCategory.MEDIA, "완결",
            RatingContentCategory.FOOD, "판매종료",
            RatingContentCategory.MUSIC, "종료",
            RatingContentCategory.BOOKS, "완결",
            RatingContentCategory.GAME, "서비스 종료",
            RatingContentCategory.PLACE, "종료"));

    private final Map<RatingContentCategory, String> labels;

    RatingContentStatus(Map<RatingContentCategory, String> labels) {
        this.labels = labels;
    }

    /** 대분류에 맞는 화면 이름 */
    public String labelFor(RatingContentCategory category) {
        return labels.get(category);
    }
}
