package com.trendizm.backend.item.application.dto;

/**
 * 이전/다음 글. 공개 질문 글을 id(등록) 순으로 놓았을 때 바로 앞뒤 글이다.
 * previous는 먼저 올라온 글, next는 나중에 올라온 글이고, 없으면 null이다.
 * 목록이 최신순이라 화면에서는 previous가 목록의 아래쪽(더 오래된) 글이다.
 */
public record ItemAdjacentQuestions(ItemQuestionLinkResponse previous, ItemQuestionLinkResponse next) {
}
