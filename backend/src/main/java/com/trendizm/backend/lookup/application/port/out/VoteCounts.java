package com.trendizm.backend.lookup.application.port.out;

/** 의견의 찬성·반대 수. 엔티티 대신 칼럼만 읽어서 영속성 컨텍스트를 거치지 않고 항상 DB 값이 나온다 */
public record VoteCounts(int agreeCount, int disagreeCount) {
}
