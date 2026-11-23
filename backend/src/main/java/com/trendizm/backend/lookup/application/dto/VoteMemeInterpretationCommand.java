package com.trendizm.backend.lookup.application.dto;

import com.trendizm.backend.lookup.domain.enums.MemeVoteType;

/** 의견 투표. 같은 쪽에 다시 투표하면 바뀌는 것이 없고, 다른 쪽이면 투표를 바꾼다 */
public record VoteMemeInterpretationCommand(Long interpretationId, Long userId, MemeVoteType type) {
}
