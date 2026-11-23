package com.trendizm.backend.lookup.application.port.in;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.lookup.application.dto.CreateMemeInterpretationCommand;
import com.trendizm.backend.lookup.application.dto.MemeInterpretationResponse;
import com.trendizm.backend.lookup.application.exception.MemeErrorCode;
import com.trendizm.backend.lookup.application.port.MemeInterpretationService;
import com.trendizm.backend.lookup.application.port.out.MemeInterpretationRepository;
import com.trendizm.backend.lookup.application.port.out.MemeRepository;
import com.trendizm.backend.lookup.domain.entity.Meme;
import com.trendizm.backend.lookup.domain.entity.MemeInterpretation;
import com.trendizm.backend.member.application.dto.MemberInfo;
import com.trendizm.backend.member.application.port.MemberService;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemeInterpretationServiceImpl implements MemeInterpretationService {

    /** 탈퇴했거나 없는 회원의 표시 이름 (item·rating과 같은 문구) */
    static final String UNKNOWN_AUTHOR = "탈퇴한 회원";

    private final MemeRepository memeRepository;
    private final MemeInterpretationRepository interpretationRepository;
    private final MemberService memberService;

    /** 문서 생성과 의견 저장이 한 트랜잭션이라, 의견 저장이 실패하면 새 문서도 롤백되어 빈 문서가 남지 않는다 */
    @Override
    @Transactional
    public MemeInterpretationResponse createInterpretation(CreateMemeInterpretationCommand command) {
        // 숨김 문서도 찾아야 한다. 공개 문서만 찾으면 숨김 단어를 새로 저장하려다 word UNIQUE 위반이 난다
        Meme meme = memeRepository.findByWord(Meme.normalizeWord(command.word()))
                .orElseGet(() -> createMeme(command));
        if (meme.isHidden()) {
            throw new BusinessException(MemeErrorCode.MEME_NOT_FOUND);
        }
        MemeInterpretation interpretation = interpretationRepository.save(MemeInterpretation.create(
                meme.getId(), command.userId(), command.meaning(), command.content()));
        return MemeInterpretationResponse.from(interpretation)
                .withAuthor(nicknames(Set.of(command.userId())).get(command.userId()));
    }

    private Meme createMeme(CreateMemeInterpretationCommand command) {
        Meme meme = Meme.create(command.word(), command.userId(), command.sourceUrl(), command.title(), command.thumbnailUrl());
        try {
            // 위 조회와 저장 사이에 다른 요청이 같은 단어로 문서를 만들면 DB word UNIQUE에서 막힌다
            return memeRepository.saveAndFlush(meme);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(MemeErrorCode.MEME_ALREADY_EXISTS);
        }
    }

    @Override
    public PageResponse<MemeInterpretationResponse> getInterpretations(Long memeId, int page, int size) {
        Meme meme = memeRepository.findById(memeId)
                .filter(found -> !found.isHidden())
                .orElseThrow(() -> new BusinessException(MemeErrorCode.MEME_NOT_FOUND));
        Page<MemeInterpretation> interpretations =
                interpretationRepository.findByMemeIdOrderByTrust(meme.getId(), PageRequest.of(page, size));
        Map<Long, String> nicknames = nicknames(
                interpretations.getContent().stream().map(MemeInterpretation::getUserId).toList());
        return PageResponse.from(interpretations,
                interpretation -> MemeInterpretationResponse.from(interpretation)
                        .withAuthor(nicknames.get(interpretation.getUserId())));
    }

    /** 회원 id → 닉네임. 목록의 작성자를 한 번에 가져온다. 탈퇴했거나 없는 회원은 UNKNOWN_AUTHOR */
    private Map<Long, String> nicknames(Collection<Long> userIds) {
        Set<Long> ids = new HashSet<>(userIds);
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, MemberInfo> members = memberService.getMembers(ids);
        return ids.stream().collect(Collectors.toMap(Function.identity(), id -> displayName(members.get(id))));
    }

    private static String displayName(MemberInfo member) {
        return member == null || member.withdrawn() ? UNKNOWN_AUTHOR : member.nickname();
    }
}
