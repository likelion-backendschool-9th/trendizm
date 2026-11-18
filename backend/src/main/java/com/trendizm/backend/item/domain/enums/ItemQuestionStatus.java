package com.trendizm.backend.item.domain.enums;

/** 질문 글 노출 상태 (ERD item_questions.status, varchar(30)). 일반 조회는 PUBLIC만 보여 준다 */
public enum ItemQuestionStatus {

    PUBLIC("게시 중"),
    HIDDEN("숨김");

    private final String label;

    ItemQuestionStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
