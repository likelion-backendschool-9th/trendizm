package com.trendizm.backend.lookup.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.lookup.application.dto.MemeInterpretationResponse;
import com.trendizm.backend.lookup.application.dto.VoteMemeInterpretationCommand;
import com.trendizm.backend.lookup.application.exception.MemeErrorCode;
import com.trendizm.backend.lookup.application.port.MemeInterpretationService;
import com.trendizm.backend.lookup.application.port.out.MemeInterpretationRepository;
import com.trendizm.backend.lookup.application.port.out.MemeInterpretationVoteRepository;
import com.trendizm.backend.lookup.application.port.out.MemeRepository;
import com.trendizm.backend.lookup.domain.entity.Meme;
import com.trendizm.backend.lookup.domain.entity.MemeInterpretation;
import com.trendizm.backend.lookup.domain.entity.MemeInterpretationVote;
import com.trendizm.backend.lookup.domain.enums.MemeVoteType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemeInterpretationVoteServiceTest {

    private static final Long WRITER = 1L;
    private static final Long VOTER = 2L;
    private static final Long OTHER_VOTER = 3L;

    @Autowired
    private MemeInterpretationService interpretationService;

    @Autowired
    private MemeRepository memeRepository;

    @Autowired
    private MemeInterpretationRepository interpretationRepository;

    @Autowired
    private MemeInterpretationVoteRepository voteRepository;

    private Long memeId;
    private Long interpretationId;
    private Long hiddenInterpretationId;

    /** 공개 문서 tmi의 의견 1개, 숨김 문서 알숨김의 의견 1개. 테스트가 끝나면 롤백된다 */
    @BeforeEach
    void setUp() {
        memeId = memeRepository.save(Meme.create("TMI", WRITER, "https://youtu.be/tmi01", null, null)).getId();
        interpretationId = interpretationRepository.save(
                MemeInterpretation.create(memeId, WRITER, "Too Much Information", "내용")).getId();

        Meme hidden = Meme.create("알숨김", WRITER, "https://example.com/hidden", null, null);
        hidden.hide();
        Long hiddenMemeId = memeRepository.save(hidden).getId();
        hiddenInterpretationId = interpretationRepository.save(
                MemeInterpretation.create(hiddenMemeId, WRITER, "숨김 의견", "내용")).getId();
    }

    private MemeInterpretationResponse vote(Long userId, MemeVoteType type) {
        return interpretationService.vote(new VoteMemeInterpretationCommand(interpretationId, userId, type));
    }

    @Test
    @DisplayName("처음 투표하면 그쪽 수가 1 늘고 myVote에 내 투표가 나온다")
    void 처음_투표() {
        MemeInterpretationResponse response = vote(VOTER, MemeVoteType.AGREE);

        assertThat(response.agreeCount()).isEqualTo(1);
        assertThat(response.disagreeCount()).isZero();
        assertThat(response.myVote()).isEqualTo("agree");
    }

    @Test
    @DisplayName("같은 쪽에 다시 투표하면 수가 바뀌지 않는다")
    void 같은_쪽_재투표() {
        vote(VOTER, MemeVoteType.AGREE);

        MemeInterpretationResponse response = vote(VOTER, MemeVoteType.AGREE);

        assertThat(response.agreeCount()).isEqualTo(1);
        assertThat(response.myVote()).isEqualTo("agree");
    }

    @Test
    @DisplayName("다른 쪽으로 바꾸면 이전 쪽은 1 줄고 새 쪽은 1 는다")
    void 투표_변경() {
        vote(VOTER, MemeVoteType.AGREE);
        vote(OTHER_VOTER, MemeVoteType.AGREE);

        MemeInterpretationResponse response = vote(VOTER, MemeVoteType.DISAGREE);

        assertThat(response.agreeCount()).isEqualTo(1);
        assertThat(response.disagreeCount()).isEqualTo(1);
        assertThat(response.myVote()).isEqualTo("disagree");
        assertThat(voteRepository.findByInterpretationIdAndUserId(interpretationId, VOTER))
                .get().extracting(MemeInterpretationVote::getType).isEqualTo(MemeVoteType.DISAGREE);
    }

    @Test
    @DisplayName("자기 의견에도 투표할 수 있다")
    void 자기_의견_투표() {
        MemeInterpretationResponse response = vote(WRITER, MemeVoteType.AGREE);

        assertThat(response.agreeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("취소하면 그쪽 수가 1 줄고 myVote가 비고, 투표가 없을 때 취소해도 그대로다")
    void 투표_취소() {
        vote(VOTER, MemeVoteType.DISAGREE);

        MemeInterpretationResponse canceled = interpretationService.cancelVote(interpretationId, VOTER);
        MemeInterpretationResponse again = interpretationService.cancelVote(interpretationId, VOTER);

        assertThat(canceled.disagreeCount()).isZero();
        assertThat(canceled.myVote()).isNull();
        assertThat(again.disagreeCount()).isZero();
        assertThat(voteRepository.findByInterpretationIdAndUserId(interpretationId, VOTER)).isEmpty();
    }

    @Test
    @DisplayName("목록의 myVote는 로그인한 사람의 투표만 보여 주고, 비로그인이면 null")
    void 목록_내_투표() {
        vote(VOTER, MemeVoteType.AGREE);

        assertThat(interpretationService.getInterpretations(memeId, VOTER, 0, 20).content())
                .extracting(MemeInterpretationResponse::myVote).containsExactly("agree");
        assertThat(interpretationService.getInterpretations(memeId, OTHER_VOTER, 0, 20).content())
                .extracting(MemeInterpretationResponse::myVote).containsExactly((String) null);
        assertThat(interpretationService.getInterpretations(memeId, null, 0, 20).content())
                .extracting(MemeInterpretationResponse::myVote).containsExactly((String) null);
    }

    @Test
    @DisplayName("없는 의견이나 숨김 문서의 의견에는 투표·취소할 수 없다 (MEME_INTERPRETATION_NOT_FOUND)")
    void 투표_대상_없음() {
        for (Long id : List.of(-1L, hiddenInterpretationId)) {
            assertThatThrownBy(() -> interpretationService.vote(
                    new VoteMemeInterpretationCommand(id, VOTER, MemeVoteType.AGREE)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(MemeErrorCode.MEME_INTERPRETATION_NOT_FOUND);
            assertThatThrownBy(() -> interpretationService.cancelVote(id, VOTER))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(MemeErrorCode.MEME_INTERPRETATION_NOT_FOUND);
        }
    }
}
