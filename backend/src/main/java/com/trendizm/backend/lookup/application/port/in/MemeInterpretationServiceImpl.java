package com.trendizm.backend.lookup.application.port.in;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.lookup.application.dto.AddMemeInterpretationCommand;
import com.trendizm.backend.lookup.application.dto.CreateMemeInterpretationCommand;
import com.trendizm.backend.lookup.application.dto.MemeCreatedEvent;
import com.trendizm.backend.lookup.application.dto.MemeInterpretationAddedEvent;
import com.trendizm.backend.lookup.application.dto.MemeInterpretationResponse;
import com.trendizm.backend.lookup.application.dto.MemeInterpretationVotedEvent;
import com.trendizm.backend.lookup.application.dto.MemeOpinionsChangedEvent;
import com.trendizm.backend.lookup.application.dto.MemeVoteResult;
import com.trendizm.backend.lookup.application.dto.VoteMemeInterpretationCommand;
import com.trendizm.backend.lookup.application.exception.MemeErrorCode;
import com.trendizm.backend.lookup.application.port.MemeInterpretationService;
import com.trendizm.backend.lookup.application.port.out.MemeInterpretationRepository;
import com.trendizm.backend.lookup.application.port.out.MemeInterpretationVoteRepository;
import com.trendizm.backend.lookup.application.port.out.MemeRepository;
import com.trendizm.backend.lookup.application.port.out.MyVote;
import com.trendizm.backend.lookup.application.port.out.VoteCounts;
import com.trendizm.backend.lookup.domain.entity.Meme;
import com.trendizm.backend.lookup.domain.entity.MemeInterpretation;
import com.trendizm.backend.lookup.domain.enums.MemeVoteType;
import com.trendizm.backend.lookup.domain.vo.SourceUrl;
import com.trendizm.backend.member.application.dto.MemberInfo;
import com.trendizm.backend.member.application.port.MemberService;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final MemeInterpretationVoteRepository voteRepository;
    private final MemberService memberService;
    /** 커밋 뒤 처리할 이벤트를 발행한다: 알림(notification)·썸네일 저장(MemeThumbnailServiceImpl). lookup은 알림 도메인을 직접 부르지 않는다 */
    private final ApplicationEventPublisher eventPublisher;

    /** 문서 생성과 의견 저장이 한 트랜잭션이라, 의견 저장이 실패하면 새 문서도 롤백되어 빈 문서가 남지 않는다 */
    @Override
    @Transactional
    public MemeInterpretationResponse createInterpretation(CreateMemeInterpretationCommand command) {
        String word = Meme.normalizeWord(command.word());
        // 요청 검증(100자)은 바꾸기 전 길이라, 소문자로 바꾸며 길어진 단어는 여기서 막는다.
        // 막지 않으면 DB 길이 오류가 아래 중복 처리(MEME_ALREADY_EXISTS)로 잘못 바뀐다
        // @NotBlank는 폭 없는 공백 같은 보이지 않는 문자를 공백으로 보지 않아서, 지운 뒤에 다시 확인한다
        if (word.isEmpty()) {
            throw new BusinessException(MemeErrorCode.MEME_WORD_BLANK);
        }
        if (word.length() > Meme.MAX_WORD_LENGTH) {
            throw new BusinessException(MemeErrorCode.MEME_WORD_TOO_LONG);
        }
        String sourceUrl = SourceUrl.normalize(command.sourceUrl());
        if (sourceUrl.length() > Meme.MAX_SOURCE_URL_LENGTH) {
            throw new BusinessException(MemeErrorCode.MEME_SOURCE_URL_TOO_LONG);
        }
        // 숨김 문서도 찾아야 한다. 공개 문서만 찾으면 숨김 문서를 새로 저장하려다 (word, source_url) UNIQUE 위반이 난다
        Meme meme = memeRepository.findByWordAndSourceUrl(word, sourceUrl)
                .orElseGet(() -> createMeme(command));
        if (meme.isHidden()) {
            throw new BusinessException(MemeErrorCode.MEME_NOT_FOUND);
        }
        return saveInterpretation(meme, command.userId(), command.meaning(), command.content());
    }

    @Override
    @Transactional
    public MemeInterpretationResponse addInterpretation(AddMemeInterpretationCommand command) {
        Meme meme = memeRepository.findById(command.memeId())
                .filter(found -> !found.isHidden())
                .orElseThrow(() -> new BusinessException(MemeErrorCode.MEME_NOT_FOUND));
        return saveInterpretation(meme, command.userId(), command.meaning(), command.content());
    }

    /**
     * 문서 주인에게 새 해석 알림이 가도록 이벤트를 발행한다. 주인이 직접 쓴 해석이면 알림 쪽에서 거른다.
     * 의견이 늘었으니 AI 요약도 다시 만든다(MemeOpinionsChangedEvent).
     */
    private MemeInterpretationResponse saveInterpretation(Meme meme, Long userId, String meaning, String content) {
        MemeInterpretation interpretation =
                interpretationRepository.save(MemeInterpretation.create(meme.getId(), userId, meaning, content));
        eventPublisher.publishEvent(new MemeInterpretationAddedEvent(
                meme.getId(), interpretation.getId(), meme.getUserId(), userId, meme.getWord()));
        eventPublisher.publishEvent(new MemeOpinionsChangedEvent(meme.getId()));
        return MemeInterpretationResponse.from(interpretation).withAuthor(nicknames(Set.of(userId)).get(userId));
    }

    private Meme createMeme(CreateMemeInterpretationCommand command) {
        Meme meme = Meme.create(command.word(), command.userId(), command.sourceUrl(), command.title());
        try {
            // 위 조회와 저장 사이에 다른 요청이 같은 단어·출처로 문서를 만들면 DB (word, source_url) UNIQUE에서 막힌다
            Meme saved = memeRepository.saveAndFlush(meme);
            // 썸네일은 이 트랜잭션이 커밋된 뒤 따로 받아 저장한다(MemeThumbnailServiceImpl). 롤백되면 실행되지 않는다
            eventPublisher.publishEvent(new MemeCreatedEvent(saved.getId()));
            return saved;
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(MemeErrorCode.MEME_ALREADY_EXISTS);
        }
    }

    @Override
    public PageResponse<MemeInterpretationResponse> getInterpretations(Long memeId, Long viewerId, int page, int size) {
        Meme meme = memeRepository.findById(memeId)
                .filter(found -> !found.isHidden())
                .orElseThrow(() -> new BusinessException(MemeErrorCode.MEME_NOT_FOUND));
        Page<MemeInterpretation> interpretations =
                interpretationRepository.findByMemeIdOrderByTrust(meme.getId(), PageRequest.of(page, size));
        List<MemeInterpretation> content = interpretations.getContent();
        Map<Long, String> nicknames = nicknames(content.stream().map(MemeInterpretation::getUserId).toList());
        Map<Long, String> myVotes = myVotes(viewerId, content.stream().map(MemeInterpretation::getId).toList());
        return PageResponse.from(interpretations,
                interpretation -> MemeInterpretationResponse.from(interpretation)
                        .withAuthor(nicknames.get(interpretation.getUserId()))
                        .withMyVote(myVotes.get(interpretation.getId())));
    }

    /**
     * 투표 수는 실제로 바뀐 행이 있을 때만 고친다(insertIfAbsent·changeType의 반환값).
     * 그래서 같은 사람이 동시에 여러 번 보내도 수가 한 번만 바뀐다.
     * 동의가 새로 들어왔을 때만(처음 동의, 비동의 → 동의) 해석 작성자에게 알림 이벤트를 발행한다.
     * 투표가 실제로 바뀌면(동의·비동의 모두) 신뢰도 순서가 달라질 수 있어서 AI 요약 이벤트도 발행한다.
     */
    @Override
    @Transactional
    public MemeVoteResult vote(VoteMemeInterpretationCommand command) {
        MemeInterpretation interpretation = interpretationRepository.findById(command.interpretationId())
                .orElseThrow(() -> new BusinessException(MemeErrorCode.MEME_INTERPRETATION_NOT_FOUND));
        // 공개 여부를 확인하며 읽은 문서를 알림 이벤트의 단어에도 쓴다(다시 조회하지 않는다)
        Meme meme = getVisibleMeme(interpretation);
        Long interpretationId = interpretation.getId();
        MemeVoteType type = command.type();
        boolean agree = type == MemeVoteType.AGREE;
        boolean changed = false;
        if (voteRepository.insertIfAbsent(interpretationId, command.userId(), type.name()) == 1) {
            interpretationRepository.addVoteCounts(interpretationId, agree ? 1 : 0, agree ? 0 : 1);
            changed = true;
        } else if (voteRepository.changeType(interpretationId, command.userId(), type) == 1) {
            interpretationRepository.addVoteCounts(interpretationId, agree ? 1 : -1, agree ? -1 : 1);
            changed = true;
        }
        MemeVoteResult result = currentState(interpretationId, command.userId());
        if (changed) {
            eventPublisher.publishEvent(new MemeOpinionsChangedEvent(meme.getId()));
        }
        if (changed && agree) {
            eventPublisher.publishEvent(new MemeInterpretationVotedEvent(meme.getId(), interpretationId,
                    interpretation.getUserId(), command.userId(), meme.getWord(), result.agreeCount()));
        }
        return result;
    }

    @Override
    @Transactional
    public MemeVoteResult cancelVote(Long interpretationId, Long userId) {
        MemeInterpretation interpretation = getVisibleInterpretation(interpretationId);
        Long id = interpretation.getId();
        voteRepository.findMyVote(id, userId).ifPresent(vote -> {
            boolean agree = vote.type() == MemeVoteType.AGREE;
            // 조회한 뒤 다른 요청이 먼저 지우거나 바꿨으면 0이 나오고 수를 고치지 않는다
            if (voteRepository.deleteByInterpretationIdAndUserIdAndType(id, userId, vote.type()) == 1) {
                interpretationRepository.addVoteCounts(id, agree ? -1 : 0, agree ? 0 : -1);
                eventPublisher.publishEvent(new MemeOpinionsChangedEvent(interpretation.getMemeId()));
            }
        });
        return currentState(id, userId);
    }

    /** 의견이 없거나 그 의견의 문서가 숨김이면 MEME_INTERPRETATION_NOT_FOUND */
    private MemeInterpretation getVisibleInterpretation(Long interpretationId) {
        MemeInterpretation interpretation = interpretationRepository.findById(interpretationId)
                .orElseThrow(() -> new BusinessException(MemeErrorCode.MEME_INTERPRETATION_NOT_FOUND));
        getVisibleMeme(interpretation);
        return interpretation;
    }

    /** 해석이 달린 문서. 문서가 없거나 숨겨졌으면 해석도 없는 것으로 본다(MEME_INTERPRETATION_NOT_FOUND) */
    private Meme getVisibleMeme(MemeInterpretation interpretation) {
        return memeRepository.findById(interpretation.getMemeId())
                .filter(meme -> !meme.isHidden())
                .orElseThrow(() -> new BusinessException(MemeErrorCode.MEME_INTERPRETATION_NOT_FOUND));
    }

    /**
     * 처리 후의 수와 내 투표. 엔티티 대신 projection으로 읽어서, 바뀐 행이 없어 영속성 컨텍스트가 비워지지 않았거나
     * 그사이 다른 요청이 투표를 바꿨어도 트랜잭션 초반에 읽은 값이 아니라 DB 값을 돌려준다
     */
    private MemeVoteResult currentState(Long interpretationId, Long userId) {
        VoteCounts counts = interpretationRepository.findVoteCountsById(interpretationId)
                .orElseThrow(() -> new BusinessException(MemeErrorCode.MEME_INTERPRETATION_NOT_FOUND));
        String myVote = voteRepository.findMyVote(interpretationId, userId).map(vote -> vote.type().slug()).orElse(null);
        return new MemeVoteResult(interpretationId, counts.agreeCount(), counts.disagreeCount(), myVote);
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

    /** 의견 id → 내 투표 slug. 비로그인이거나 투표하지 않은 의견은 없다 */
    private Map<Long, String> myVotes(Long viewerId, Collection<Long> interpretationIds) {
        if (viewerId == null || interpretationIds.isEmpty()) {
            return Map.of();
        }
        return voteRepository.findMyVotes(viewerId, interpretationIds).stream()
                .collect(Collectors.toMap(MyVote::interpretationId, vote -> vote.type().slug()));
    }
}
