package com.trendizm.backend.item.application.dto;

import com.trendizm.backend.item.domain.entity.ItemQuestion;
import java.time.LocalDateTime;

/**
 * 질문 글 목록의 한 줄. contentPreview는 본문 앞부분(최대 100자)이다.
 * 작성자 닉네임과 개수는 서비스에서 묶음 조회로 채운다: from(question).withAuthor(...).withCounts(...)
 */
public record ItemQuestionSummaryResponse(
        Long id,
        ItemCategoryInfo category,
        ItemCategoryInfo subcategory,
        String title,
        String contentPreview,
        String authorNickname,
        long commentCount,
        long recommendationCount,
        LocalDateTime createdAt
) {

    static final int PREVIEW_LENGTH = 100;

    public static ItemQuestionSummaryResponse from(ItemQuestion question) {
        return new ItemQuestionSummaryResponse(
                question.getId(),
                ItemCategoryInfo.of(question.getCategory()),
                ItemCategoryInfo.of(question.getSubcategory()),
                question.getTitle(),
                preview(question.getContent()),
                null,
                0,
                0,
                question.getCreatedAt());
    }

    public ItemQuestionSummaryResponse withAuthor(String authorNickname) {
        return new ItemQuestionSummaryResponse(id, category, subcategory, title, contentPreview,
                authorNickname, commentCount, recommendationCount, createdAt);
    }

    public ItemQuestionSummaryResponse withCounts(long commentCount, long recommendationCount) {
        return new ItemQuestionSummaryResponse(id, category, subcategory, title, contentPreview,
                authorNickname, commentCount, recommendationCount, createdAt);
    }

    private static String preview(String content) {
        // 서로게이트 쌍(이모지 등)을 반으로 자르지 않도록 코드 포인트 기준으로 자른다
        if (content.codePointCount(0, content.length()) <= PREVIEW_LENGTH) {
            return content;
        }
        return content.substring(0, content.offsetByCodePoints(0, PREVIEW_LENGTH));
    }
}
