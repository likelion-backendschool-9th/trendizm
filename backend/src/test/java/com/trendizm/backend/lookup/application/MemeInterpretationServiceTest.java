package com.trendizm.backend.lookup.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.lookup.application.dto.AddMemeInterpretationCommand;
import com.trendizm.backend.lookup.application.dto.CreateMemeInterpretationCommand;
import com.trendizm.backend.lookup.application.dto.MemeInterpretationResponse;
import com.trendizm.backend.lookup.application.dto.MemeResponse;
import com.trendizm.backend.lookup.application.exception.MemeErrorCode;
import com.trendizm.backend.lookup.application.port.MemeInterpretationService;
import com.trendizm.backend.lookup.application.port.MemeService;
import com.trendizm.backend.lookup.application.port.out.MemeInterpretationRepository;
import com.trendizm.backend.lookup.application.port.out.MemeRepository;
import com.trendizm.backend.lookup.domain.entity.Meme;
import com.trendizm.backend.lookup.domain.entity.MemeInterpretation;
import com.trendizm.backend.member.application.dto.RegisterMemberCommand;
import com.trendizm.backend.member.application.port.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemeInterpretationServiceTest {

    @Autowired
    private MemeInterpretationService interpretationService;

    @Autowired
    private MemeService memeService;

    @Autowired
    private MemeRepository memeRepository;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemeInterpretationRepository interpretationRepository;

    /** 없는 회원 id. 닉네임이 "탈퇴한 회원"으로 나온다 */
    private static final Long UNKNOWN_USER = -1L;

    private Long writer;
    private Long reader;
    private Meme tmi;
    private Meme hidden;

    /** 공개 문서 tmi와 숨김 문서 알숨김의 출처. 의견 작성 command도 TMI_SOURCE를 쓴다 */
    private static final String TMI_SOURCE = "https://youtu.be/tmi01";
    private static final String HIDDEN_SOURCE = "https://example.com/hidden";

    /** 회원 2명, 공개 문서 tmi(의견 없음), 숨김 문서 알숨김. 테스트가 끝나면 롤백된다 */
    @BeforeEach
    void setUp() {
        writer = memberService.register(new RegisterMemberCommand("writer@test.com", "encoded", "밈장인", false));
        reader = memberService.register(new RegisterMemberCommand("reader@test.com", "encoded", "TMI러", false));
        tmi = memeRepository.save(Meme.create("TMI", 1L, TMI_SOURCE, null, null));
        hidden = Meme.create("알숨김", 1L, HIDDEN_SOURCE, null, null);
        hidden.hide();
        memeRepository.save(hidden);
    }

    private CreateMemeInterpretationCommand command(String word, String sourceUrl) {
        return new CreateMemeInterpretationCommand(word, writer, "뜻", "내용", sourceUrl, "영상 제목", null);
    }

    private CreateMemeInterpretationCommand command(String word) {
        return command(word, "https://video.example.com/watch/01");
    }

    @Test
    @DisplayName("문서가 없는 단어·출처로 첫 의견을 쓰면 문서가 함께 만들어지고, 출처는 문서에 저장된다")
    void 첫_의견으로_문서_생성() {
        MemeInterpretationResponse created = interpretationService.createInterpretation(command(" 알잘딱깔센 "));

        MemeResponse meme = memeService.getMeme(created.memeId());
        assertThat(created.memeId()).isEqualTo(meme.id());
        assertThat(created.userId()).isEqualTo(writer);
        assertThat(created.authorNickname()).isEqualTo("밈장인");
        assertThat(created.agreeCount()).isZero();
        assertThat(created.disagreeCount()).isZero();
        assertThat(meme.sourceUrl()).isEqualTo("https://video.example.com/watch/01");
        assertThat(meme.title()).isEqualTo("영상 제목");
    }

    @Test
    @DisplayName("같은 단어·출처면 대소문자나 모바일·웹 링크가 달라도 있는 문서에 붙고, 제목은 바꾸지 않는다")
    void 같은_단어_출처면_기존_문서() {
        MemeInterpretationResponse created =
                interpretationService.createInterpretation(command("Tmi", "https://m.youtube.com/watch?v=tmi01&t=3s"));

        assertThat(created.memeId()).isEqualTo(tmi.getId());
        assertThat(memeService.getMeme(tmi.getId()).title()).isNull();
        assertThat(memeService.getMemesByWord("tmi", null, 0, 20).totalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("같은 단어라도 출처가 다르면 새 문서를 만든다")
    void 출처가_다르면_새_문서() {
        MemeInterpretationResponse created =
                interpretationService.createInterpretation(command("TMI", "https://x.com/someone/status/1"));

        assertThat(created.memeId()).isNotEqualTo(tmi.getId());
        assertThat(memeService.getMemesByWord("tmi", null, 0, 20).content()).extracting(MemeResponse::id)
                .containsExactly(created.memeId(), tmi.getId());
    }

    @Test
    @DisplayName("고른 문서에 의견을 쓸 수 있고, 없거나 숨김인 문서면 MEME_NOT_FOUND")
    void 고른_문서에_작성() {
        MemeInterpretationResponse created = interpretationService.addInterpretation(
                new AddMemeInterpretationCommand(tmi.getId(), reader, "뜻", "내용"));

        assertThat(created.memeId()).isEqualTo(tmi.getId());
        assertThat(created.authorNickname()).isEqualTo("TMI러");
        for (Long memeId : new Long[] {-1L, hidden.getId()}) {
            assertThatThrownBy(() -> interpretationService.addInterpretation(
                    new AddMemeInterpretationCommand(memeId, reader, "뜻", "내용")))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(MemeErrorCode.MEME_NOT_FOUND);
        }
    }

    @Test
    @DisplayName("정규화한 출처 주소가 500자를 넘으면 MEME_SOURCE_URL_TOO_LONG (http를 https로 바꾸면 한 글자 는다)")
    void 정규화로_길어지는_주소() {
        String url = "http://example.com/" + "a".repeat(500 - "http://example.com/".length());

        assertThatThrownBy(() -> interpretationService.createInterpretation(command("TMI", url)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(MemeErrorCode.MEME_SOURCE_URL_TOO_LONG);
    }

    @Test
    @DisplayName("소문자로 바꾸면 100자를 넘는 단어는 MEME_WORD_TOO_LONG (\"İ\"는 소문자로 2글자)")
    void 소문자로_길어지는_단어() {
        assertThatThrownBy(() -> interpretationService.createInterpretation(command("İ".repeat(51))))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(MemeErrorCode.MEME_WORD_TOO_LONG);

        MemeInterpretationResponse created = interpretationService.createInterpretation(command("İ".repeat(50)));
        assertThat(created.memeId()).isNotNull();
    }

    @Test
    @DisplayName("숨김 문서의 단어·출처로 쓰면 새 문서를 만들지 않고 MEME_NOT_FOUND")
    void 숨김_문서에_작성() {
        assertThatThrownBy(() -> interpretationService.createInterpretation(command("알숨김", HIDDEN_SOURCE)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(MemeErrorCode.MEME_NOT_FOUND);
    }

    @Test
    @DisplayName("목록은 찬성 - 반대가 큰 순서, 같으면 최신순")
    void 신뢰도순_목록() {
        MemeInterpretation low = saveWithVotes("반대 많음", 1, 3);   // -2
        MemeInterpretation older = saveWithVotes("먼저 쓴 1점", 2, 1); // 1
        MemeInterpretation newer = saveWithVotes("나중 쓴 1점", 1, 0); // 1
        MemeInterpretation top = saveWithVotes("찬성 많음", 5, 0);   // 5

        PageResponse<MemeInterpretationResponse> page = interpretationService.getInterpretations(tmi.getId(), 0, 20);

        assertThat(page.content()).extracting(MemeInterpretationResponse::id)
                .containsExactly(top.getId(), newer.getId(), older.getId(), low.getId());
    }

    @Test
    @DisplayName("목록에 작성자 닉네임을 붙이고, 없는 회원은 '탈퇴한 회원'으로 보여 준다")
    void 작성자_닉네임() {
        saveWithVotes(writer, "밈장인 의견", 2, 0);
        saveWithVotes(reader, "TMI러 의견", 1, 0);
        saveWithVotes(UNKNOWN_USER, "탈퇴 회원 의견", 0, 0);

        PageResponse<MemeInterpretationResponse> page = interpretationService.getInterpretations(tmi.getId(), 0, 20);

        assertThat(page.content()).extracting(MemeInterpretationResponse::authorNickname)
                .containsExactly("밈장인", "TMI러", "탈퇴한 회원");
    }

    @Test
    @DisplayName("목록도 페이지 크기만큼 나눠서 보여 준다")
    void 목록_페이지() {
        for (int i = 0; i < 3; i++) {
            saveWithVotes("뜻" + i, 0, 0);
        }

        PageResponse<MemeInterpretationResponse> page = interpretationService.getInterpretations(tmi.getId(), 1, 2);

        assertThat(page.content()).hasSize(1);
        assertThat(page.totalElements()).isEqualTo(3);
        assertThat(page.hasNext()).isFalse();
    }

    @Test
    @DisplayName("없거나 숨김인 문서의 의견 목록은 MEME_NOT_FOUND")
    void 없는_문서_목록() {
        for (Long memeId : new Long[] {-1L, hidden.getId()}) {
            assertThatThrownBy(() -> interpretationService.getInterpretations(memeId, 0, 20))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(MemeErrorCode.MEME_NOT_FOUND);
        }
    }

    private MemeInterpretation saveWithVotes(String meaning, int agree, int disagree) {
        return saveWithVotes(writer, meaning, agree, disagree);
    }

    /** 투표 기능 전이라 찬성·반대 수는 필드에 직접 넣는다 */
    private MemeInterpretation saveWithVotes(Long userId, String meaning, int agree, int disagree) {
        MemeInterpretation interpretation = MemeInterpretation.create(tmi.getId(), userId, meaning, "내용");
        ReflectionTestUtils.setField(interpretation, "agreeCount", agree);
        ReflectionTestUtils.setField(interpretation, "disagreeCount", disagree);
        return interpretationRepository.save(interpretation);
    }
}
