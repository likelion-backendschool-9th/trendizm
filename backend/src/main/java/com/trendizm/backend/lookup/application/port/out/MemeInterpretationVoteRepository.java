package com.trendizm.backend.lookup.application.port.out;

import com.trendizm.backend.lookup.domain.entity.MemeInterpretationVote;
import com.trendizm.backend.lookup.domain.enums.MemeVoteType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** 구현체는 adapter/out/persistence/JpaMemeInterpretationVoteRepository. 반환한 int는 실제로 바뀐 행 수(0 또는 1)다 */
public interface MemeInterpretationVoteRepository {

    MemeInterpretationVote save(MemeInterpretationVote vote);

    /** 이미 투표했으면 넣지 않고 0 */
    int insertIfAbsent(Long interpretationId, Long userId, String type);

    /** 지금 투표가 type과 다를 때만 type으로 바꾼다. 같거나 투표가 없으면 0 */
    int changeType(Long interpretationId, Long userId, MemeVoteType type);

    /** 지금 투표가 type일 때만 지운다 */
    int deleteByInterpretationIdAndUserIdAndType(Long interpretationId, Long userId, MemeVoteType type);

    /** 이 회원의 이 의견 투표. 엔티티를 거치지 않아 항상 DB 값이 나온다 */
    Optional<MyVote> findMyVote(Long interpretationId, Long userId);

    /** 목록·투표 응답에 내 투표(myVote)를 표시할 때 쓴다. 엔티티를 거치지 않아 항상 DB 값이 나온다 */
    List<MyVote> findMyVotes(Long userId, Collection<Long> interpretationIds);
}
