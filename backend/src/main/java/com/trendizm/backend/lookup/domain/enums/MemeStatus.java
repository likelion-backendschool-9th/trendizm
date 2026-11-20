package com.trendizm.backend.lookup.domain.enums;

/**
 * 밈 문서 공개 상태. memes.status에 이름 그대로 저장한다.
 * 숨김·해제 API는 관리자 기능(12.x)에서 추가한다.
 */
public enum MemeStatus {
    /** 공개 */
    PUBLIC,
    /** 숨김. 검색·목록·단건 조회 모두에서 보이지 않는다 */
    HIDDEN
}
