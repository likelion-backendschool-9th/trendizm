package com.trendizm.backend.rating.domain.enums;

import java.util.Locale;

/**
 * 평점 콘텐츠 대분류 (ERD rating_contents.category, varchar(30)).
 * slug는 화면 주소에 쓰는 이름이다. 예: /rating/cate/media
 */
public enum RatingContentCategory {

    MEDIA("미디어"),
    FOOD("푸드"),
    MUSIC("음악"),
    BOOKS("도서·웹툰"),
    GAME("게임"),
    PLACE("장소");

    private final String label;

    RatingContentCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** 예: MEDIA → media */
    public String getSlug() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
