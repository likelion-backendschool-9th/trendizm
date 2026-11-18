package com.trendizm.backend.member.domain;

/**
 * 회원 이용 상태. users.status에 이름 그대로 저장한다.
 * 정지·해제 처리와 정지 회원 로그인 차단은 관리자 기능(12.x)에서 추가한다.
 */
public enum MemberStatus {
    /** 활성 */
    ACTIVE,
    /** 이용 제한 */
    SUSPENDED
}
