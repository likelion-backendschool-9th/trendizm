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

    /** 대소문자를 무시하고 찾는다(TMI와 tmi는 같은 문서) */
    Optional<Meme> findByWordIgnoreCaseAndStatus(String word, MemeStatus status);

    Page<Meme> findByStatus(MemeStatus status, Pageable pageable);

    /** 대소문자를 무시하고 단어에 keyword가 들어간 문서를 찾는다 */
    Page<Meme> findByStatusAndWordContainingIgnoreCase(MemeStatus status, String keyword, Pageable pageable);
}
