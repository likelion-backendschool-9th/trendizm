package com.trendizm.backend.rating.domain.enums;

import java.util.Locale;

/**
 * 평점 콘텐츠 소분류 (ERD rating_contents.subcategory, varchar(30)).
 * 소분류마다 대분류가 정해져 있어서, 콘텐츠를 만들 때 소분류만 받고 대분류는 여기서 채운다.
 * slug는 화면 주소에 쓰는 이름이다. 예: /rating/cate/music/concert-festival
 */
public enum RatingContentSubcategory {

    MOVIE(RatingContentCategory.MEDIA, "영화"),
    DRAMA(RatingContentCategory.MEDIA, "드라마"),
    VARIETY(RatingContentCategory.MEDIA, "예능"),
    ANIMATION(RatingContentCategory.MEDIA, "애니메이션"),

    DISH(RatingContentCategory.FOOD, "음식"),
    DESSERT(RatingContentCategory.FOOD, "디저트"),
    BEVERAGE(RatingContentCategory.FOOD, "음료"),
    NEW_PRODUCT(RatingContentCategory.FOOD, "편의점·프랜차이즈 신상"),

    ALBUM(RatingContentCategory.MUSIC, "앨범"),
    SONG(RatingContentCategory.MUSIC, "노래"),
    CONCERT_FESTIVAL(RatingContentCategory.MUSIC, "공연·페스티벌"),

    BOOK(RatingContentCategory.BOOKS, "책"),
    WEBTOON(RatingContentCategory.BOOKS, "웹툰"),
    WEB_NOVEL(RatingContentCategory.BOOKS, "웹소설"),

    PC_CONSOLE(RatingContentCategory.GAME, "PC·콘솔"),
    MOBILE(RatingContentCategory.GAME, "모바일"),

    CAFE(RatingContentCategory.PLACE, "카페"),
    EXHIBITION_POPUP(RatingContentCategory.PLACE, "전시·팝업"),
    TRAVEL(RatingContentCategory.PLACE, "여행지");

    private final RatingContentCategory category;
    private final String label;

    RatingContentSubcategory(RatingContentCategory category, String label) {
        this.category = category;
        this.label = label;
    }

    public RatingContentCategory getCategory() {
        return category;
    }

    public String getLabel() {
        return label;
    }

    /** 예: CONCERT_FESTIVAL → concert-festival */
    public String getSlug() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
