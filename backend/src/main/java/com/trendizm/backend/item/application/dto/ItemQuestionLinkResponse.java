package com.trendizm.backend.item.application.dto;

import com.trendizm.backend.item.domain.entity.ItemQuestion;

/** 다른 질문 글로 가는 링크 (이전/다음 글). 예: { "id": 3, "title": "양말 추천해주세요" } */
public record ItemQuestionLinkResponse(Long id, String title) {

    public static ItemQuestionLinkResponse from(ItemQuestion question) {
        return new ItemQuestionLinkResponse(question.getId(), question.getTitle());
    }
}
