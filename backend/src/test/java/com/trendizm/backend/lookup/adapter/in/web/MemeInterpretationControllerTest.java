package com.trendizm.backend.lookup.adapter.in.web;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.trendizm.backend.auth.adapter.out.security.CustomUserDetails;
import com.trendizm.backend.auth.adapter.out.security.SecurityConfig;
import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.lookup.application.dto.CreateMemeInterpretationCommand;
import com.trendizm.backend.lookup.application.dto.MemeInterpretationResponse;
import com.trendizm.backend.lookup.application.exception.MemeErrorCode;
import com.trendizm.backend.lookup.application.port.MemeInterpretationService;
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
    private static final MemeInterpretationResponse INTERPRETATION = new MemeInterpretationResponse(
            1L, 3L, 7L, "알아서 잘 딱 깔끔하고 센스 있게", "내용", 0, 0, LocalDateTime.now(), LocalDateTime.now());

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemeInterpretationService interpretationService;

    @Test
    @DisplayName("로그인하면 의견을 쓸 수 있다. '/'·'%'가 든 단어도 본문으로 받고, 빈 출처 값은 null로 넘긴다")
    void 의견_작성() throws Exception {
        CreateMemeInterpretationCommand command = new CreateMemeInterpretationCommand(
                "50%/할인", 7L, "알아서 잘 딱 깔끔하고 센스 있게", "내용", "https://video.example.com/watch/01", null, null);
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
    @DisplayName("단어나 뜻이 비어 있으면 400 INVALID_INPUT")
    void 필수값_누락() throws Exception {
        for (String body : List.of("{\"word\":\" \",\"meaning\":\"뜻\",\"content\":\"내용\"}",
                "{\"word\":\"TMI\",\"meaning\":\" \",\"content\":\"내용\"}")) {
            mockMvc.perform(post(CREATE).with(user(LOGIN_USER)).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        }
        verifyNoInteractions(interpretationService);
    }

    @Test
    @DisplayName("단어가 100자를 넘으면 400 INVALID_INPUT")
    void 긴_단어() throws Exception {
        mockMvc.perform(post(CREATE).with(user(LOGIN_USER)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"word\":\"" + "가".repeat(101) + "\",\"meaning\":\"뜻\",\"content\":\"내용\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        verifyNoInteractions(interpretationService);
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
    @DisplayName("의견 목록은 로그인 없이 볼 수 있다")
    void 목록() throws Exception {
        given(interpretationService.getInterpretations(3L, 0, 20))
                .willReturn(new PageResponse<>(List.of(INTERPRETATION), 0, 20, 1, 1, false));

        mockMvc.perform(get(LIST, 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].meaning").value("알아서 잘 딱 깔끔하고 센스 있게"));
    }

    @Test
    @DisplayName("없는 문서의 의견 목록은 404 MEME_NOT_FOUND")
    void 없는_문서_목록() throws Exception {
        given(interpretationService.getInterpretations(99L, 0, 20))
                .willThrow(new BusinessException(MemeErrorCode.MEME_NOT_FOUND));

        mockMvc.perform(get(LIST, 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MEME_NOT_FOUND"));
    }
}
