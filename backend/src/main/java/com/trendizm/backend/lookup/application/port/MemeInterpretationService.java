package com.trendizm.backend.lookup.application.port;

import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.lookup.application.dto.CreateMemeInterpretationCommand;
import com.trendizm.backend.lookup.application.dto.MemeInterpretationResponse;

/** 밈 의견 작성·조회 */
public interface MemeInterpretationService {

    /**
     * 의견을 쓴다. 그 단어의 문서가 없으면 이 의견과 함께 문서를 만든다(문서와 의견을 한 트랜잭션으로 저장).
     * 문서는 상태와 관계없이 먼저 찾고, 숨김 상태면 MEME_NOT_FOUND
     */
    MemeInterpretationResponse createInterpretation(CreateMemeInterpretationCommand command);

    /** 공개 문서의 의견 목록 (신뢰도순: 찬성 - 반대가 큰 순, 같으면 최신순). 문서가 없거나 숨김이면 MEME_NOT_FOUND */
    PageResponse<MemeInterpretationResponse> getInterpretations(Long memeId, int page, int size);
}
