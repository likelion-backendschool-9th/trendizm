package com.trendizm.backend.lookup.application.port;

import com.trendizm.backend.lookup.application.dto.MemeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** 컬럼이 정해지면 createMeme·updateMeme에 파라미터를 추가한다. */
public interface MemeService {

    Page<MemeResponse> getMemes(Pageable pageable);

    /** 없으면 예외를 던진다 */
    MemeResponse getMeme(Long memeId);

    /** @return 생성한 밈 id */
    Long createMeme();

    void updateMeme(Long memeId);

    void deleteMeme(Long memeId);
}
