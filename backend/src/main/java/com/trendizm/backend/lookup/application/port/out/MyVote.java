package com.trendizm.backend.lookup.application.port.out;

import com.trendizm.backend.lookup.domain.enums.MemeVoteType;

/** 한 회원이 한 의견에 한 투표. 엔티티 대신 칼럼만 읽어서 영속성 컨텍스트를 거치지 않고 항상 DB 값이 나온다 */
public record MyVote(Long interpretationId, MemeVoteType type) {
}
