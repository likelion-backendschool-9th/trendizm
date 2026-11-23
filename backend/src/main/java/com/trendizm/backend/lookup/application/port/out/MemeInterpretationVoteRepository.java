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

    Optional<MemeInterpretationVote> findByInterpretationIdAndUserId(Long interpretationId, Long userId);

    /** 목록에 내 투표(myVote)를 표시할 때 쓴다 */
    List<MemeInterpretationVote> findByUserIdAndInterpretationIdIn(Long userId, Collection<Long> interpretationIds);
}
