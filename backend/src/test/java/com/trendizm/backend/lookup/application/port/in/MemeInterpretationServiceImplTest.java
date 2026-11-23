package com.trendizm.backend.lookup.application.port.in;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.lookup.application.dto.CreateMemeInterpretationCommand;
import com.trendizm.backend.lookup.application.exception.MemeErrorCode;
import com.trendizm.backend.lookup.application.port.out.MemeInterpretationRepository;
import com.trendizm.backend.lookup.application.port.out.MemeRepository;
import com.trendizm.backend.lookup.domain.entity.Meme;
import com.trendizm.backend.member.application.port.MemberService;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

/** 동시 요청은 통합 테스트로 재현하기 어려워서, 저장소를 mock으로 바꿔 UNIQUE 위반 상황만 확인한다 */
class MemeInterpretationServiceImplTest {

    private final MemeRepository memeRepository = mock(MemeRepository.class);
    private final MemeInterpretationRepository interpretationRepository = mock(MemeInterpretationRepository.class);
    private final MemberService memberService = mock(MemberService.class);
    private final MemeInterpretationServiceImpl service =
            new MemeInterpretationServiceImpl(memeRepository, interpretationRepository, memberService);

    @Test
    @DisplayName("조회와 저장 사이에 다른 요청이 같은 단어로 문서를 만들면 409 MEME_ALREADY_EXISTS")
    void 동시_문서_생성() {
        given(memeRepository.findByWord("알잘딱깔센")).willReturn(Optional.empty());
        given(memeRepository.saveAndFlush(any(Meme.class))).willThrow(new DataIntegrityViolationException("uk_memes_word"));

        assertThatThrownBy(() -> service.createInterpretation(
                new CreateMemeInterpretationCommand("알잘딱깔센", 2L, "뜻", "내용", null, null, null)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(MemeErrorCode.MEME_ALREADY_EXISTS);
        verifyNoInteractions(interpretationRepository);
    }
}
