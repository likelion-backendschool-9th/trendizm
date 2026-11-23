package com.trendizm.backend.lookup.application.port;

import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.lookup.application.dto.MemeResponse;

/**
 * 밈 문서 조회. 숨김 문서는 어디에서도 보이지 않는다.
 * 같은 단어라도 출처가 다르면 다른 문서다. 단어로 찾으면 출처별 문서 목록을 주고, 화면에서 하나를 고른다.
 * 문서 생성은 첫 의견 작성 때 함께 한다(MemeInterpretationService.createInterpretation).
 */
public interface MemeService {

    /**
     * 공개 문서 목록 (최근 등록순). keyword가 비어 있지 않으면 단어에 keyword가 들어간 문서만(대소문자 무시).
     * page는 0부터 시작한다.
     */
    PageResponse<MemeResponse> getMemes(String keyword, int page, int size);

    /** 단어가 같은 공개 문서 목록 (출처별, 최근 등록순). 대소문자 무시, 띄어쓰기는 구분. 없으면 빈 목록 */
    PageResponse<MemeResponse> getMemesByWord(String word, int page, int size);

    /** 문서 하나. 없거나 숨김이면 MEME_NOT_FOUND */
    MemeResponse getMeme(Long memeId);
}
