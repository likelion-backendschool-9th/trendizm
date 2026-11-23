package com.trendizm.backend.lookup.adapter.out.persistence;

import com.trendizm.backend.lookup.application.port.out.MemeInterpretationVoteRepository;
import com.trendizm.backend.lookup.domain.entity.MemeInterpretationVote;
import com.trendizm.backend.lookup.domain.enums.MemeVoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** MemeInterpretationVoteRepository(out 포트)의 메서드를 Spring Data가 구현한다 */
public interface JpaMemeInterpretationVoteRepository
        extends JpaRepository<MemeInterpretationVote, Long>, MemeInterpretationVoteRepository {

    /**
     * (interpretation_id, user_id) 유니크 제약에 걸리면 넣지 않는다. 충돌 대상을 적지 않은 형태는 H2도 지원한다.
     * 네이티브 INSERT라 JPA Auditing이 동작하지 않아서 created_at·updated_at을 직접 채운다.
     */
    @Override
    @Modifying
    @Query(value = """
            INSERT INTO meme_interpretation_votes (interpretation_id, user_id, type, created_at, updated_at)
            VALUES (:interpretationId, :userId, :type, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("interpretationId") Long interpretationId, @Param("userId") Long userId,
                       @Param("type") String type);

    @Override
    @Modifying
    @Query("""
            update MemeInterpretationVote v set v.type = :type, v.updatedAt = CURRENT_TIMESTAMP
            where v.interpretationId = :interpretationId and v.userId = :userId and v.type <> :type
            """)
    int changeType(@Param("interpretationId") Long interpretationId, @Param("userId") Long userId,
                   @Param("type") MemeVoteType type);

    @Override
    @Modifying
    @Query("""
            delete from MemeInterpretationVote v
            where v.interpretationId = :interpretationId and v.userId = :userId and v.type = :type
            """)
    int deleteByInterpretationIdAndUserIdAndType(@Param("interpretationId") Long interpretationId,
                                                 @Param("userId") Long userId, @Param("type") MemeVoteType type);
}
