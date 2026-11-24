package com.trendizm.backend.lookup.adapter.out.persistence;

import com.trendizm.backend.lookup.application.port.out.MemeInterpretationRepository;
import com.trendizm.backend.lookup.application.port.out.VoteCounts;
import com.trendizm.backend.lookup.domain.entity.MemeInterpretation;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** MemeInterpretationRepository(out 포트)의 메서드를 Spring Data가 구현한다. 서비스는 out 포트만 알고 이 인터페이스는 모른다. */
public interface JpaMemeInterpretationRepository
        extends JpaRepository<MemeInterpretation, Long>, MemeInterpretationRepository {

    /** 찬성 - 반대는 계산 값이라 메서드 이름 정렬로 만들 수 없어 JPQL로 쓴다 */
    @Override
    @Query("""
            select i from MemeInterpretation i
            where i.memeId = :memeId
            order by (i.agreeCount - i.disagreeCount) desc, i.createdAt desc, i.id desc
            """)
    Page<MemeInterpretation> findByMemeIdOrderByTrust(@Param("memeId") Long memeId, Pageable pageable);

    /** 바꾼 뒤 영속성 컨텍스트를 비워서, 같은 트랜잭션에 남아 있던 엔티티가 옛 수를 들고 있지 않게 한다 */
    @Override
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update MemeInterpretation i
            set i.agreeCount = i.agreeCount + :agreeDelta, i.disagreeCount = i.disagreeCount + :disagreeDelta
            where i.id = :id
            """)
    void addVoteCounts(@Param("id") Long id, @Param("agreeDelta") int agreeDelta,
                       @Param("disagreeDelta") int disagreeDelta);

    @Override
    @Query("""
            select new com.trendizm.backend.lookup.application.port.out.VoteCounts(i.agreeCount, i.disagreeCount)
            from MemeInterpretation i
            where i.id = :id
            """)
    Optional<VoteCounts> findVoteCountsById(@Param("id") Long id);
}
