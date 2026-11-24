package com.trendizm.backend.lookup.application.dto;

/**
 * 투표·취소 결과. 화면은 이 값으로 동의·비동의 수와 내 투표 표시를 바꾼다.
 * myVote는 "agree", "disagree", 또는 null(투표 안 함)
 */
public record MemeVoteResult(
        Long interpretationId,
        int agreeCount,
        int disagreeCount,
        String myVote
) {
}
