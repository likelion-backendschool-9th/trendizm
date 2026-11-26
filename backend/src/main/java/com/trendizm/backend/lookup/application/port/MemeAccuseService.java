package com.trendizm.backend.lookup.application.port;

import com.trendizm.backend.accuse.application.dto.AccuseResponse;
import com.trendizm.backend.lookup.application.dto.CreateMemeAccuseCommand;

/** 밈 문서 신고. 문서를 확인한 뒤 신고 저장은 accuse 도메인(AccuseService)에 맡긴다 */
public interface MemeAccuseService {

    /**
     * 공개 문서를 신고한다. 문서가 없거나 숨김이면 MEME_NOT_FOUND. 문서를 처음 만든 회원(memes.user_id)은 신고할 수 없다.
     * 자기 글·사유·내용 검사와 중복 신고(ACCUSE_OWN_TARGET, ACCUSE_ALREADY_EXISTS 등)는 AccuseService의 규칙을 따른다
     */
    AccuseResponse accuseMeme(CreateMemeAccuseCommand command);
}
