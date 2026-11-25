package com.trendizm.backend.lookup.application.dto;

/**
 * 문서에 해석이 새로 저장됨. 커밋된 뒤 알림(notification)이 받는다.
 *
 * @param memeOwnerId 문서를 처음 등록한 회원 (알림을 받는 사람)
 * @param authorId 해석을 쓴 회원
 * @param word 문서 단어 (알림 문구에 쓴다)
 */
public record MemeInterpretationAddedEvent(
        Long memeId,
        Long interpretationId,
        Long memeOwnerId,
        Long authorId,
        String word
) {
}
