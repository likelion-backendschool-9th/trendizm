package com.trendizm.backend.lookup.application.port;

import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.lookup.application.dto.AddMemeInterpretationCommand;
import com.trendizm.backend.lookup.application.dto.CreateMemeInterpretationCommand;
import com.trendizm.backend.lookup.application.dto.MemeInterpretationResponse;

/** 밈 의견 작성·조회 */
public interface MemeInterpretationService {

    /**
     * 단어·출처로 의견을 쓴다. 같은 단어·출처의 문서가 없으면 이 의견과 함께 문서를 만든다(한 트랜잭션).
     * 문서는 상태와 관계없이 먼저 찾고, 숨김 상태면 MEME_NOT_FOUND.
     * 정규화한 단어·주소가 칼럼 길이를 넘으면 MEME_WORD_TOO_LONG·MEME_SOURCE_URL_TOO_LONG,
     * 같은 단어·출처로 동시에 문서가 만들어져 DB에서 막히면 MEME_ALREADY_EXISTS
     */
    MemeInterpretationResponse createInterpretation(CreateMemeInterpretationCommand command);

    /** 이미 있는 문서에 의견을 쓴다. 문서가 없거나 숨김이면 MEME_NOT_FOUND */
    MemeInterpretationResponse addInterpretation(AddMemeInterpretationCommand command);

    /** 공개 문서의 의견 목록 (신뢰도순: 찬성 - 반대가 큰 순, 같으면 최신순). 문서가 없거나 숨김이면 MEME_NOT_FOUND */
    PageResponse<MemeInterpretationResponse> getInterpretations(Long memeId, int page, int size);
}
