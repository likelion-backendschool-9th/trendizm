package com.trendizm.backend.lookup.adapter.in.web.dto;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.lookup.application.dto.VoteMemeInterpretationCommand;
import com.trendizm.backend.lookup.application.exception.MemeErrorCode;
import com.trendizm.backend.lookup.domain.enums.MemeVoteType;
import jakarta.validation.constraints.NotBlank;

/** 의견 투표. type은 "agree"(동의) 또는 "disagree"(비동의). 예: { "type": "agree" } */
public record VoteMemeInterpretationRequest(
        @NotBlank(message = "투표 종류를 입력해 주세요.")
        String type
) {

    /** 없는 종류면 MEME_INVALID_VOTE_TYPE */
    public VoteMemeInterpretationCommand toCommand(Long interpretationId, Long userId) {
        MemeVoteType parsed = MemeVoteType.fromSlug(type)
                .orElseThrow(() -> new BusinessException(MemeErrorCode.MEME_INVALID_VOTE_TYPE));
        return new VoteMemeInterpretationCommand(interpretationId, userId, parsed);
    }
}
