package com.trendizm.backend.member.application.port.in;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.member.application.dto.MemberInfo;
import com.trendizm.backend.member.application.exception.MemberErrorCode;
import com.trendizm.backend.member.application.port.MemberService;
import com.trendizm.backend.member.application.port.out.MemberRepository;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
}
