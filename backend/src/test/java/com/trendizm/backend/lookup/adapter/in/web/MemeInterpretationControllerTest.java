package com.trendizm.backend.lookup.adapter.in.web;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.trendizm.backend.auth.adapter.out.security.CustomUserDetails;
import com.trendizm.backend.auth.adapter.out.security.SecurityConfig;
import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.lookup.application.dto.AddMemeInterpretationCommand;
import com.trendizm.backend.lookup.application.dto.CreateMemeInterpretationCommand;
import com.trendizm.backend.lookup.application.dto.MemeInterpretationResponse;
import com.trendizm.backend.lookup.application.dto.MemeVoteResult;
import com.trendizm.backend.lookup.application.dto.VoteMemeInterpretationCommand;
import com.trendizm.backend.lookup.application.exception.MemeErrorCode;
import com.trendizm.backend.lookup.application.port.MemeInterpretationService;
import com.trendizm.backend.lookup.domain.enums.MemeVoteType;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MemeInterpretationController.class)
@Import(SecurityConfig.class)
class MemeInterpretationControllerTest {

    private static final CustomUserDetails LOGIN_USER = new CustomUserDetails(7L, "member@test.com", "encoded", List.of());
    private static final String CREATE = "/api/lookup/interpretations";
    private static final String LIST = "/api/lookup/memes/{memeId}/interpretations";
    private static final String VOTE = "/api/lookup/interpretations/{interpretationId}/vote";
    private static final String DETAIL = "/api/lookup/interpretations/{interpretationId}";
    private static final MemeInterpretationResponse INTERPRETATION = new MemeInterpretationResponse(
            1L, 3L, 7L, "밈장인", "알아서 잘 딱 깔끔하고 센스 있게", "내용", 0, 0, null, LocalDateTime.now(), LocalDateTime.now());

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemeInterpretationService interpretationService;

    @Test
    @DisplayName("로그인하면 의견을 쓸 수 있다. '/'·'%'가 든 단어도 본문으로 받고, 빈 출처 값은 null로 넘긴다")
    void 의견_작성() throws Exception {
        CreateMemeInterpretationCommand command = new CreateMemeInterpretationCommand(
                "50%/할인", 7L, "알아서 잘 딱 깔끔하고 센스 있게", "내용", "https://video.example.com/watch/01", null);
        given(interpretationService.createInterpretation(command)).willReturn(INTERPRETATION);

        mockMvc.perform(post(CREATE).with(user(LOGIN_USER)).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"word":"50%/할인","meaning":" 알아서 잘 딱 깔끔하고 센스 있게 ","content":"내용",
                                 "sourceUrl":"https://video.example.com/watch/01","title":" "}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.memeId").value(3));
    }

    @Test
    @DisplayName("단어·뜻·출처 주소가 비어 있으면 400 INVALID_INPUT")
    void 필수값_누락() throws Exception {
        for (String body : List.of(
                "{\"word\":\" \",\"meaning\":\"뜻\",\"content\":\"내용\",\"sourceUrl\":\"https://example.com/1\"}",
                "{\"word\":\"TMI\",\"meaning\":\" \",\"content\":\"내용\",\"sourceUrl\":\"https://example.com/1\"}",
                "{\"word\":\"TMI\",\"meaning\":\"뜻\",\"content\":\"내용\"}",
                "{\"word\":\"TMI\",\"meaning\":\"뜻\",\"content\":\"내용\",\"sourceUrl\":\" \"}")) {
            mockMvc.perform(post(CREATE).with(user(LOGIN_USER)).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        }
        verifyNoInteractions(interpretationService);
    }

    @Test
    @DisplayName("단어·뜻·내용이 보이지 않는 글자만 있거나, 단어·뜻·내용·제목에 NULL 같은 제어 문자가 있으면 400 INVALID_INPUT")
    void 보이지_않는_글과_제어_문자() throws Exception {
        String template = "{\"word\":\"%s\",\"meaning\":\"%s\",\"content\":\"%s\","
                + "\"sourceUrl\":\"https://example.com/1\",\"title\":\"%s\"}";
        List<String> bad = List.of("\\u3164", "\\u200B", "\\u00A0\\u3000", "\\u2800", "내용\\u0000", "\\u0007경고", "a\\u001Bb");
        for (String value : bad) {
            for (String body : List.of(
                    template.formatted(value, "뜻", "내용", "제목"),
                    template.formatted("TMI", value, "내용", "제목"),
                    template.formatted("TMI", "뜻", value, "제목"))) {
                mockMvc.perform(post(CREATE).with(user(LOGIN_USER)).contentType(MediaType.APPLICATION_JSON).content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
            }
            for (String body : List.of(
                    "{\"meaning\":\"" + value + "\",\"content\":\"내용\"}",
                    "{\"meaning\":\"뜻\",\"content\":\"" + value + "\"}")) {
                mockMvc.perform(post(LIST, 3L).with(user(LOGIN_USER)).contentType(MediaType.APPLICATION_JSON).content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
            }
        }
        for (String control : List.of("내용\\u0000", "\\u0007경고", "a\\u001Bb")) {
            mockMvc.perform(post(CREATE).with(user(LOGIN_USER)).contentType(MediaType.APPLICATION_JSON)
                            .content(template.formatted("TMI", "뜻", "내용", control)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        }
        verifyNoInteractions(interpretationService);
    }

    @Test
    @DisplayName("출처 주소가 http(s)://가 아니면 400 INVALID_INPUT (javascript: 주소로 XSS 방지)")
    void 잘못된_주소() throws Exception {
        for (String field : List.of("sourceUrl")) {
            for (String url : List.of("javascript:alert(1)", "data:text/html,<script>alert(1)</script>", "ftp://example.com/a",
                    "https://example.com/a b", "https://youtu.be/abc\u200B", "https://example.com/ㅤ")) {
                mockMvc.perform(post(CREATE).with(user(LOGIN_USER)).contentType(MediaType.APPLICATION_JSON)
                                .content(("{\"word\":\"TMI\",\"meaning\":\"뜻\",\"content\":\"내용\","
                                        + "\"sourceUrl\":\"https://example.com/1\",\"%s\":\"%s\"}")
                                        .formatted(field, url)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
            }
        }
        verifyNoInteractions(interpretationService);
    }

    @Test
    @DisplayName("주소는 대소문자와 관계없이 http(s)://면 받는다. 썸네일 주소를 보내도 쓰지 않는다(서버가 출처에서 가져온다)")
    void 허용되는_주소() throws Exception {
        CreateMemeInterpretationCommand command = new CreateMemeInterpretationCommand(
                "TMI", 7L, "뜻", "내용", "HTTP://example.com/a", null);
        given(interpretationService.createInterpretation(command)).willReturn(INTERPRETATION);

        mockMvc.perform(post(CREATE).with(user(LOGIN_USER)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"word\":\"TMI\",\"meaning\":\"뜻\",\"content\":\"내용\","
                                + "\"sourceUrl\":\"HTTP://example.com/a\",\"thumbnailUrl\":\"https://evil.example/a.png\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("단어가 100자를 넘으면 400 INVALID_INPUT")
    void 긴_단어() throws Exception {
        mockMvc.perform(post(CREATE).with(user(LOGIN_USER)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"word\":\"" + "가".repeat(101)
                                + "\",\"meaning\":\"뜻\",\"content\":\"내용\",\"sourceUrl\":\"https://example.com/1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        verifyNoInteractions(interpretationService);
    }

    @Test
    @DisplayName("목록에서 고른 문서에 의견을 쓴다")
    void 고른_문서에_작성() throws Exception {
        given(interpretationService.addInterpretation(new AddMemeInterpretationCommand(3L, 7L, "뜻", "내용")))
                .willReturn(INTERPRETATION);

        mockMvc.perform(post(LIST, 3L).with(user(LOGIN_USER)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"meaning\":\" 뜻 \",\"content\":\"내용\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memeId").value(3));
        mockMvc.perform(post(LIST, 3L).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"meaning\":\"뜻\",\"content\":\"내용\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그인하지 않으면 의견을 쓸 수 없다 (401)")
    void 비로그인_작성() throws Exception {
        mockMvc.perform(post(CREATE).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"word\":\"TMI\",\"meaning\":\"뜻\",\"content\":\"내용\"}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(interpretationService);
    }

    @Test
    @DisplayName("의견 목록은 로그인 없이 볼 수 있다 (viewerId는 null)")
    void 목록() throws Exception {
        given(interpretationService.getInterpretations(3L, null, 0, 20))
                .willReturn(new PageResponse<>(List.of(INTERPRETATION), 0, 20, 1, 1, false));

        mockMvc.perform(get(LIST, 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].meaning").value("알아서 잘 딱 깔끔하고 센스 있게"))
                .andExpect(jsonPath("$.content[0].authorNickname").value("밈장인"));
    }

    @Test
    @DisplayName("없는 문서의 의견 목록은 404 MEME_NOT_FOUND")
    void 없는_문서_목록() throws Exception {
        given(interpretationService.getInterpretations(99L, null, 0, 20))
                .willThrow(new BusinessException(MemeErrorCode.MEME_NOT_FOUND));

        mockMvc.perform(get(LIST, 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MEME_NOT_FOUND"));
    }

    @Test
    @DisplayName("로그인하고 목록을 보면 viewerId를 넘긴다 (myVote 표시용)")
    void 로그인_목록() throws Exception {
        given(interpretationService.getInterpretations(3L, 7L, 0, 20))
                .willReturn(new PageResponse<>(List.of(INTERPRETATION.withMyVote("agree")), 0, 20, 1, 1, false));

        mockMvc.perform(get(LIST, 3L).with(user(LOGIN_USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].myVote").value("agree"));
    }

    @Test
    @DisplayName("의견 하나는 로그인 없이 볼 수 있다 (viewerId는 null)")
    void 단건() throws Exception {
        given(interpretationService.getInterpretation(1L, null)).willReturn(INTERPRETATION);

        mockMvc.perform(get(DETAIL, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.memeId").value(3))
                .andExpect(jsonPath("$.authorNickname").value("밈장인"))
                .andExpect(jsonPath("$.myVote").isEmpty());
    }

    @Test
    @DisplayName("로그인하고 의견 하나를 보면 viewerId를 넘긴다 (myVote 표시용)")
    void 로그인_단건() throws Exception {
        given(interpretationService.getInterpretation(1L, 7L)).willReturn(INTERPRETATION.withMyVote("disagree"));

        mockMvc.perform(get(DETAIL, 1L).with(user(LOGIN_USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.myVote").value("disagree"));
    }

    @Test
    @DisplayName("없는 의견은 404 MEME_INTERPRETATION_NOT_FOUND")
    void 없는_의견_단건() throws Exception {
        given(interpretationService.getInterpretation(99L, null))
                .willThrow(new BusinessException(MemeErrorCode.MEME_INTERPRETATION_NOT_FOUND));

        mockMvc.perform(get(DETAIL, 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MEME_INTERPRETATION_NOT_FOUND"));
    }

    @Test
    @DisplayName("의견 id가 숫자가 아니면 400 (서비스를 부르지 않는다)")
    void 잘못된_의견_id() throws Exception {
        mockMvc.perform(get(DETAIL, "abc"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(interpretationService);
    }

    @Test
    @DisplayName("로그인하면 투표할 수 있다. 종류는 대소문자를 무시한다")
    void 투표() throws Exception {
        given(interpretationService.vote(new VoteMemeInterpretationCommand(1L, 7L, MemeVoteType.DISAGREE)))
                .willReturn(new MemeVoteResult(1L, 2, 3, "disagree"));

        mockMvc.perform(put(VOTE, 1L).with(user(LOGIN_USER)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"Disagree\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interpretationId").value(1))
                .andExpect(jsonPath("$.agreeCount").value(2))
                .andExpect(jsonPath("$.disagreeCount").value(3))
                .andExpect(jsonPath("$.myVote").value("disagree"))
                .andExpect(jsonPath("$.content").doesNotExist());
    }

    @Test
    @DisplayName("투표 종류가 agree·disagree가 아니면 400 MEME_INVALID_VOTE_TYPE")
    void 잘못된_투표_종류() throws Exception {
        mockMvc.perform(put(VOTE, 1L).with(user(LOGIN_USER)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"like\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MEME_INVALID_VOTE_TYPE"));
        verifyNoInteractions(interpretationService);
    }

    @Test
    @DisplayName("로그인하면 투표를 취소할 수 있다")
    void 투표_취소() throws Exception {
        given(interpretationService.cancelVote(1L, 7L)).willReturn(new MemeVoteResult(1L, 2, 2, null));

        mockMvc.perform(delete(VOTE, 1L).with(user(LOGIN_USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.myVote").isEmpty());
    }

    @Test
    @DisplayName("로그인하지 않으면 투표·취소할 수 없다 (401)")
    void 비로그인_투표() throws Exception {
        mockMvc.perform(put(VOTE, 1L).contentType(MediaType.APPLICATION_JSON).content("{\"type\":\"agree\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete(VOTE, 1L))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(interpretationService);
    }
}
