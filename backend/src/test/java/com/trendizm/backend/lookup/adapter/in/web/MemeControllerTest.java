package com.trendizm.backend.lookup.adapter.in.web;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.trendizm.backend.auth.adapter.out.security.SecurityConfig;
import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.lookup.application.dto.MemeResponse;
import com.trendizm.backend.lookup.application.exception.MemeErrorCode;
import com.trendizm.backend.lookup.application.port.MemeService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MemeController.class)
@Import(SecurityConfig.class)
class MemeControllerTest {

    private static final MemeResponse MEME = new MemeResponse(
            1L, "알잘딱깔센", "https://video.example.com/watch/01", "영상 제목", null, LocalDateTime.now());

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemeService memeService;

    @Test
    @DisplayName("로그인 없이 단어로 문서를 조회한다 (한글 단어는 URL 인코딩해서 보낸다)")
    void 단어로_조회() throws Exception {
        given(memeService.getMeme("알잘딱깔센")).willReturn(MEME);

        mockMvc.perform(get("/api/lookup/memes/{word}", "알잘딱깔센"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.word").value("알잘딱깔센"))
                .andExpect(jsonPath("$.title").value("영상 제목"));
    }

    @Test
    @DisplayName("없는 단어는 404 MEME_NOT_FOUND")
    void 없는_단어() throws Exception {
        given(memeService.getMeme("없는단어")).willThrow(new BusinessException(MemeErrorCode.MEME_NOT_FOUND));

        mockMvc.perform(get("/api/lookup/memes/{word}", "없는단어"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MEME_NOT_FOUND"));
    }

    @Test
    @DisplayName("목록은 keyword와 페이지 값을 서비스에 넘긴다")
    void 목록() throws Exception {
        given(memeService.getMemes("알", 1, 10))
                .willReturn(new PageResponse<>(List.of(MEME), 1, 10, 11, 2, false));

        mockMvc.perform(get("/api/lookup/memes").param("keyword", "알").param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].word").value("알잘딱깔센"))
                .andExpect(jsonPath("$.page").value(1));
    }

    @Test
    @DisplayName("size가 100을 넘으면 400 INVALID_INPUT")
    void 잘못된_페이지_크기() throws Exception {
        mockMvc.perform(get("/api/lookup/memes").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        verifyNoInteractions(memeService);
    }

    @Test
    @DisplayName("조회(GET)만 공개다. 로그인 없이 POST하면 401")
    void 조회_외에는_로그인_필요() throws Exception {
        mockMvc.perform(post("/api/lookup/memes"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(memeService);
    }
}
