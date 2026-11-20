package com.trendizm.backend.rating.domain.enums;

/** 관리자 숨김 (ERD rating_contents.visibility, varchar(30)). 일반 조회는 PUBLIC만 보여 준다 */
public enum RatingContentVisibility {

    PUBLIC("공개"),
    HIDDEN("숨김");

    private final String label;

    RatingContentVisibility(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
