package com.trendizm.backend.lookup.application.port.out;

import com.trendizm.backend.lookup.domain.entity.MemeInterpretation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemeInterpretationRepository {

    MemeInterpretation save(MemeInterpretation interpretation);

    /** 문서의 의견을 신뢰도순(찬성 - 반대 내림차순, 같으면 최신순)으로. pageable의 정렬은 쓰지 않는다 */
    Page<MemeInterpretation> findByMemeIdOrderByTrust(Long memeId, Pageable pageable);
}
