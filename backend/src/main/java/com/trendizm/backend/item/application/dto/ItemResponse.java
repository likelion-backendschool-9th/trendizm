package com.trendizm.backend.item.application.dto;

import com.trendizm.backend.item.domain.entity.Item;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 추천 아이템 하나.
 * 잠겨 있으면(locked = true) 아이템을 알아볼 수 있는 정보(title, seller, description, imageUrl, linkUrl)는 null이다.
 * 가격·태그·추천한 사람 정보·열람 가격은 잠겨 있어도 보여 준다.
 * mine은 보는 사람이 등록자인지다. 등록자에게는 처음부터 열린 상태로 보여 준다.
 */
public record ItemResponse(
        Long id,
        Long questionId,
        String authorNickname,
        boolean mine,
        boolean locked,
        String title,
        String seller,
        Integer price,
        List<String> tags,
        String description,
        String imageUrl,
        String linkUrl,
        String targetAgeGroup,
        String targetGender,
        int unlockPrice,
        LocalDateTime createdAt
) {

    /** 열린 상태(모든 정보 포함) */
    public static ItemResponse from(Item item) {
        return new ItemResponse(item.getId(), item.getQuestionId(), null, false, false,
                item.getTitle(), item.getSeller(), item.getPrice(), item.getTags() == null ? List.of() : List.copyOf(item.getTags()),
                item.getDescription(), item.getImageUrl(), item.getLinkUrl(),
                item.getTargetAgeGroup(), item.getTargetGender(), item.getUnlockPrice(), item.getCreatedAt());
    }

    /** 잠긴 정보를 지운 사본 */
    public ItemResponse lock() {
        return new ItemResponse(id, questionId, authorNickname, mine, true,
                null, null, price, tags, null, null, null,
                targetAgeGroup, targetGender, unlockPrice, createdAt);
    }

    public ItemResponse withAuthor(String authorNickname) {
        return new ItemResponse(id, questionId, authorNickname, mine, locked, title, seller, price, tags,
                description, imageUrl, linkUrl, targetAgeGroup, targetGender, unlockPrice, createdAt);
    }

    public ItemResponse withMine(boolean mine) {
        return new ItemResponse(id, questionId, authorNickname, mine, locked, title, seller, price, tags,
                description, imageUrl, linkUrl, targetAgeGroup, targetGender, unlockPrice, createdAt);
    }
}
