package com.trendizm.backend.lookup.adapter.in.web;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.trendizm.backend.accuse.application.dto.AccuseResponse;
import com.trendizm.backend.accuse.application.exception.AccuseErrorCode;
import com.trendizm.backend.auth.adapter.out.security.CustomUserDetails;
import com.trendizm.backend.auth.adapter.out.security.SecurityConfig;
import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.lookup.application.dto.CreateMemeAccuseCommand;
import com.trendizm.backend.lookup.application.exception.MemeErrorCode;
import com.trendizm.backend.lookup.application.port.MemeAccuseService;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(MemeAccuseController.class)
@Import(SecurityConfig.class)
class MemeAccuseControllerTest {

    private static final CustomUserDetails LOGIN_USER = new CustomUserDetails(7L, "member@test.com", "encoded", List.of());
    private static final String ACCUSE = "/api/lookup/memes/{memeId}/accuses";
    private static final AccuseResponse ACCUSED =
            new AccuseResponse(1L, "meme", 3L, "hate", "비하 표현", LocalDateTime.now());

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemeAccuseService memeAccuseService;

    private static MockHttpServletRequestBuilder accuse(Object memeId, String body) {
        return post(ACCUSE, memeId).contentType(MediaType.APPLICATION_JSON).content(body);
    }

    @Test
    @DisplayName("로그인하면 문서를 신고할 수 있다 (201). 내용은 앞뒤 공백을 지워 넘긴다")
    void 신고() throws Exception {
        given(memeAccuseService.accuseMeme(new CreateMemeAccuseCommand(3L, 7L, "hate", "비하 표현")))
                .willReturn(ACCUSED);

        mockMvc.perform(accuse(3L, "{\"reason\":\"hate\",\"description\":\"  비하 표현 \"}").with(user(LOGIN_USER)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.target").value("meme"))
                .andExpect(jsonPath("$.targetId").value(3))
                .andExpect(jsonPath("$.reason").value("hate"))
                .andExpect(jsonPath("$.userId").doesNotExist());
    }

    @Test
    @DisplayName("내용이 없거나 보이지 않는 글자만 있으면 null로 넘긴다")
    void 빈_내용() throws Exception {
        given(memeAccuseService.accuseMeme(new CreateMemeAccuseCommand(3L, 7L, "ad", null))).willReturn(ACCUSED);

        mockMvc.perform(accuse(3L, "{\"reason\":\"ad\"}").with(user(LOGIN_USER)))
                .andExpect(status().isCreated());
        mockMvc.perform(accuse(3L, "{\"reason\":\"ad\",\"description\":\"\\u200B\\u3164 \"}").with(user(LOGIN_USER)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("로그인하지 않으면 신고할 수 없다 (401)")
    void 비로그인_신고() throws Exception {
        mockMvc.perform(accuse(3L, "{\"reason\":\"hate\"}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(memeAccuseService);
    }

    @Test
    @DisplayName("사유가 비었거나, 내용에 제어 문자가 있거나 500자를 넘으면 400 INVALID_INPUT")
    void 잘못된_요청() throws Exception {
        for (String body : new String[] {
                "{}",
                "{\"reason\":\" \"}",
                "{\"reason\":\"etc\",\"description\":\"광고\\u0000\"}",
                "{\"reason\":\"etc\",\"description\":\"" + "가".repeat(501) + "\"}"}) {
            mockMvc.perform(accuse(3L, body).with(user(LOGIN_USER)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        }
        verifyNoInteractions(memeAccuseService);
    }

    @Test
    @DisplayName("문서 id가 숫자가 아니면 400 (서비스를 부르지 않는다)")
    void 잘못된_문서_id() throws Exception {
        mockMvc.perform(accuse("abc", "{\"reason\":\"hate\"}").with(user(LOGIN_USER)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(memeAccuseService);
    }

    @Test
    @DisplayName("서비스의 실패는 에러 코드대로 나간다: 없는 문서 404, 없는 사유·기타 내용 누락 400, 중복 409")
    void 서비스_실패() throws Exception {
        given(memeAccuseService.accuseMeme(new CreateMemeAccuseCommand(99L, 7L, "hate", null)))
                .willThrow(new BusinessException(MemeErrorCode.MEME_NOT_FOUND));
        given(memeAccuseService.accuseMeme(new CreateMemeAccuseCommand(3L, 7L, "spam", null)))
                .willThrow(new BusinessException(AccuseErrorCode.ACCUSE_INVALID_REASON));
        given(memeAccuseService.accuseMeme(new CreateMemeAccuseCommand(3L, 7L, "etc", null)))
                .willThrow(new BusinessException(AccuseErrorCode.ACCUSE_DESCRIPTION_REQUIRED));
        given(memeAccuseService.accuseMeme(new CreateMemeAccuseCommand(3L, 7L, "hate", null)))
                .willThrow(new BusinessException(AccuseErrorCode.ACCUSE_ALREADY_EXISTS));

        mockMvc.perform(accuse(99L, "{\"reason\":\"hate\"}").with(user(LOGIN_USER)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MEME_NOT_FOUND"));
        mockMvc.perform(accuse(3L, "{\"reason\":\"spam\"}").with(user(LOGIN_USER)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ACCUSE_INVALID_REASON"));
        mockMvc.perform(accuse(3L, "{\"reason\":\"etc\"}").with(user(LOGIN_USER)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ACCUSE_DESCRIPTION_REQUIRED"));
        mockMvc.perform(accuse(3L, "{\"reason\":\"hate\"}").with(user(LOGIN_USER)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ACCUSE_ALREADY_EXISTS"));
    }
}
