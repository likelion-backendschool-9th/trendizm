package com.trendizm.backend.item.application.dto;

import com.trendizm.backend.item.domain.entity.ItemQuestion;
import java.time.LocalDateTime;

/**
 * 질문 글 상세. mine은 보는 사람이 작성자인지(화면의 ⋯ 메뉴: 수정·삭제 / 신고·차단)다. 로그인하지 않았으면 false.
 * 작성자 닉네임과 개수는 서비스에서 채운다: from(question).withAuthor(...).withCounts(...).withMine(...)
 */
public record ItemQuestionResponse(
        Long id,
        ItemCategoryInfo category,
        ItemCategoryInfo subcategory,
        String title,
        String content,
        String authorNickname,
        boolean mine,
        long commentCount,
        long recommendationCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        int viewCount
) {

    public static ItemQuestionResponse from(ItemQuestion question) {
        return new ItemQuestionResponse(
                question.getId(),
                ItemCategoryInfo.of(question.getCategory()),
                ItemCategoryInfo.of(question.getSubcategory()),
                question.getTitle(),
                question.getContent(),
                null,
                false,
                0,
                0,
                question.getCreatedAt(),
                question.getUpdatedAt(),
                question.getViewCount());
    }

    public ItemQuestionResponse withAuthor(String authorNickname) {
        return new ItemQuestionResponse(id, category, subcategory, title, content, authorNickname, mine,
                commentCount, recommendationCount, createdAt, updatedAt, viewCount);
    }

    public ItemQuestionResponse withMine(boolean mine) {
        return new ItemQuestionResponse(id, category, subcategory, title, content, authorNickname, mine,
                commentCount, recommendationCount, createdAt, updatedAt, viewCount);
    }

    public ItemQuestionResponse withCounts(long commentCount, long recommendationCount) {
        return new ItemQuestionResponse(id, category, subcategory, title, content, authorNickname, mine,
                commentCount, recommendationCount, createdAt, updatedAt, viewCount);
    }
}
