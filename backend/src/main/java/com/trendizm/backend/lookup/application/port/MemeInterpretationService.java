package com.trendizm.backend.lookup.application.port;

import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.lookup.application.dto.AddMemeInterpretationCommand;
import com.trendizm.backend.lookup.application.dto.CreateMemeInterpretationCommand;
import com.trendizm.backend.lookup.application.dto.MemeInterpretationResponse;
import com.trendizm.backend.lookup.application.dto.MemeVoteResult;
import com.trendizm.backend.lookup.application.dto.VoteMemeInterpretationCommand;

/** 밈 의견 작성·조회·투표 */
public interface MemeInterpretationService {

    /**
     * 단어·출처로 의견을 쓴다. 같은 단어·출처의 문서가 없으면 이 의견과 함께 문서를 만든다(한 트랜잭션).
     * 문서는 상태와 관계없이 먼저 찾고, 숨김 상태면 MEME_NOT_FOUND.
     * 정규화한 단어가 비면 MEME_WORD_BLANK, 단어·주소가 칼럼 길이를 넘으면 MEME_WORD_TOO_LONG·MEME_SOURCE_URL_TOO_LONG,
     * 같은 단어·출처로 동시에 문서가 만들어져 DB에서 막히면 MEME_ALREADY_EXISTS
     */
    MemeInterpretationResponse createInterpretation(CreateMemeInterpretationCommand command);

    /** 이미 있는 문서에 의견을 쓴다. 문서가 없거나 숨김이면 MEME_NOT_FOUND */
    MemeInterpretationResponse addInterpretation(AddMemeInterpretationCommand command);

    /**
     * 공개 문서의 의견 목록 (신뢰도순: 찬성 - 반대가 큰 순, 같으면 최신순). 문서가 없거나 숨김이면 MEME_NOT_FOUND.
     * viewerId는 로그인한 사용자 id(myVote 표시용)이고 비로그인이면 null
     */
    PageResponse<MemeInterpretationResponse> getInterpretations(Long memeId, Long viewerId, int page, int size);

    /**
     * 의견에 투표한다. 처음이면 저장하고, 다른 쪽에 투표해 두었으면 바꾸고, 같은 쪽이면 바뀌는 것이 없다.
     * 자기 의견에도 투표할 수 있다. 의견이 없거나 문서가 숨김이면 MEME_INTERPRETATION_NOT_FOUND.
     * 결과의 수와 myVote는 처리 후 DB에서 읽은 값이다
     */
    MemeVoteResult vote(VoteMemeInterpretationCommand command);

    /**
     * 투표를 취소한다. 투표하지 않았으면 바뀌는 것이 없다. 의견이 없거나 문서가 숨김이면 MEME_INTERPRETATION_NOT_FOUND.
     * 결과의 수와 myVote는 처리 후 DB에서 읽은 값이다
     */
    MemeVoteResult cancelVote(Long interpretationId, Long userId);
}
