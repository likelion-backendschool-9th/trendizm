package com.trendizm.backend.lookup.application.dto;

/**
 * 해석에 동의 투표가 새로 들어옴(처음 동의, 또는 비동의 → 동의). 커밋된 뒤 알림(notification)이 받는다.
 * 비동의·취소는 보내지 않는다.
 *
 * @param authorId 해석을 쓴 회원 (알림을 받는 사람)
 * @param voterId 투표한 회원
 * @param word 문서 단어 (알림 문구에 쓴다)
 * @param agreeCount 투표 처리 후의 동의 수
 */
public record MemeInterpretationVotedEvent(
        Long memeId,
        Long interpretationId,
        Long authorId,
        Long voterId,
        String word,
        int agreeCount
) {
}
