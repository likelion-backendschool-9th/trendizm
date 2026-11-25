package com.trendizm.backend.lookup.application.dto;

/**
 * 문서의 의견이나 투표가 바뀌었다(의견 추가, 동의·비동의, 투표 변경·취소). 커밋된 뒤 AI 요약을 다시 만든다(MemeSummaryServiceImpl).
 * 알림용 이벤트(MemeInterpretationAddedEvent·MemeInterpretationVotedEvent)와 따로 둔다. 알림은 동의만 받지만 요약은 모든 변화를 받는다.
 */
public record MemeOpinionsChangedEvent(Long memeId) {
}
