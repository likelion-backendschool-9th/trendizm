package com.trendizm.backend.lookup.application.port.out;

import com.trendizm.backend.lookup.domain.entity.Meme;
import com.trendizm.backend.lookup.domain.enums.MemeStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 구현체는 adapter/out/persistence/JpaMemeRepository (Spring Data가 자동 구현).
 * 메서드 이름을 Spring Data 규칙(findByXxx 등)대로 추가하면 쿼리를 따로 쓰지 않아도 된다.
 */
public interface MemeRepository {

    Meme save(Meme meme);

    Optional<Meme> findById(Long id);

    /** word는 Meme.normalizeWord로 바꿔서 넘긴다 */
    Optional<Meme> findByWordAndStatus(String word, MemeStatus status);

    Page<Meme> findByStatus(MemeStatus status, Pageable pageable);

    /** 단어에 keyword가 들어간 문서. keyword는 Meme.normalizeWord로 바꿔서 넘긴다(%, _는 Spring Data가 이스케이프한다) */
    Page<Meme> findByStatusAndWordContaining(MemeStatus status, String keyword, Pageable pageable);
}
