package com.trendizm.backend.item.domain.enums;

import java.util.Locale;

/**
 * 트렌드 아이템 대분류 (ERD item_questions.category, varchar(30)).
 * slug는 화면 주소에 쓰는 이름이다. 예: /item/cate/fashion
 */
public enum ItemCategory {

    FASHION("패션"),
    BEAUTY("뷰티"),
    LIVING("리빙"),
    HOBBY("취미"),
    DIGITAL("디지털"),
    FINANCE("재테크");

    private final String label;

    ItemCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** 예: FASHION → fashion */
    public String getSlug() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
