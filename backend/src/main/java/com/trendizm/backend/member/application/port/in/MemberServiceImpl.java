package com.trendizm.backend.member.application.port.in;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.member.application.dto.MemberInfo;
import com.trendizm.backend.member.application.dto.RegisterMemberCommand;
import com.trendizm.backend.member.application.exception.MemberErrorCode;
import com.trendizm.backend.member.application.port.MemberService;
import com.trendizm.backend.member.application.port.out.MemberRepository;
import com.trendizm.backend.member.domain.Member;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    @Override
    public MemberInfo getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .map(MemberInfo::from)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    @Override
    public Map<Long, MemberInfo> getMembers(Collection<Long> memberIds) {
        if (memberIds.isEmpty()) {
            return Map.of();
        }
        return memberRepository.findAllById(memberIds).stream()
                .map(MemberInfo::from)
                .collect(Collectors.toMap(MemberInfo::id, Function.identity()));
    }

    @Override
    @Transactional
    public Long register(RegisterMemberCommand command) {
        if (!isEmailAvailable(command.email())) {
            throw new BusinessException(MemberErrorCode.DUPLICATE_EMAIL);
        }
        if (!isNicknameAvailable(command.nickname())) {
            throw new BusinessException(MemberErrorCode.DUPLICATE_NICKNAME);
        }
        Member member = Member.create(
                command.email(), command.encodedPassword(), command.nickname(), command.marketingAgreed());
        try {
            // 위 확인과 저장 사이에 다른 요청이 같은 값으로 가입하면 DB unique 제약에서 막힌다
            return memberRepository.saveAndFlush(member).getId();
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(MemberErrorCode.DUPLICATE_MEMBER);
        }
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return !memberRepository.existsByEmail(Member.normalizeEmail(email));
    }

    @Override
    public boolean isNicknameAvailable(String nickname) {
        return !memberRepository.existsByNickname(nickname.strip());
    }
}
