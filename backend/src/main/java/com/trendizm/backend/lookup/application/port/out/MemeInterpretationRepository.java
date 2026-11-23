package com.trendizm.backend.lookup.application.port.out;

import com.trendizm.backend.lookup.domain.entity.MemeInterpretation;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemeInterpretationRepository {

    MemeInterpretation save(MemeInterpretation interpretation);

    Optional<MemeInterpretation> findById(Long id);

    /** 찬성·반대 수를 DB에서 바로 더한다(동시 투표에도 수가 틀어지지 않게). 예: 동의→비동의 변경은 (-1, +1) */
    void addVoteCounts(Long id, int agreeDelta, int disagreeDelta);

    /** 문서의 의견을 신뢰도순(찬성 - 반대 내림차순, 같으면 최신순)으로. pageable의 정렬은 쓰지 않는다 */
    Page<MemeInterpretation> findByMemeIdOrderByTrust(Long memeId, Pageable pageable);
}
