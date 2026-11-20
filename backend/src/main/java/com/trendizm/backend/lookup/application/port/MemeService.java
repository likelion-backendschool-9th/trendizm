package com.trendizm.backend.lookup.application.port;

import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.lookup.application.dto.MemeResponse;

/**
 * 밈 문서 조회. 숨김 문서는 어디에서도 보이지 않는다.
 * 문서 생성은 첫 의견 작성 때 함께 하므로 의견 작성 기능에서 추가한다.
 */
public interface MemeService {

    /**
     * 공개 문서 목록 (최근 등록순). keyword가 비어 있지 않으면 단어에 keyword가 들어간 문서만.
     * page는 0부터 시작한다.
     */
    PageResponse<MemeResponse> getMemes(String keyword, int page, int size);

    /** 단어로 공개 문서 하나를 찾는다. 없거나 숨김이면 MEME_NOT_FOUND */
    MemeResponse getMeme(String word);
}
