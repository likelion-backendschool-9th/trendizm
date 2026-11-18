package com.trendizm.backend.member.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.member.application.dto.RegisterMemberCommand;
import com.trendizm.backend.member.application.exception.MemberErrorCode;
import com.trendizm.backend.member.application.port.in.MemberServiceImpl;
import com.trendizm.backend.member.application.port.out.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

/** 동시에 같은 값으로 가입하는 상황은 실제로 만들기 어려워서, 저장소가 DB 중복 오류를 던지는 것으로 흉내 낸다 */
class MemberRegisterRaceTest {

    @Test
    @DisplayName("중복 확인을 통과했는데 DB에서 막히면 500이 아니라 409 DUPLICATE_MEMBER로 바꾼다")
    void 동시_가입() {
        MemberRepository repository = mock(MemberRepository.class);
        given(repository.existsByEmail(anyString())).willReturn(false);
        given(repository.existsByNickname(anyString())).willReturn(false);
        given(repository.saveAndFlush(any())).willThrow(new DataIntegrityViolationException("unique"));
        MemberServiceImpl service = new MemberServiceImpl(repository);

        assertThatThrownBy(() -> service.register(
                new RegisterMemberCommand("a@test.com", "encoded", "닉네임", false)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(MemberErrorCode.DUPLICATE_MEMBER);
    }
}
