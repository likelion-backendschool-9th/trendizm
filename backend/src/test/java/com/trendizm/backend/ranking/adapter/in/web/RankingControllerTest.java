package com.trendizm.backend.ranking.adapter.in.web;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.trendizm.backend.auth.adapter.out.security.SecurityConfig;
import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.ranking.application.dto.RankingCategoryResponse;
import com.trendizm.backend.ranking.application.dto.RankingLinkResponse;
import com.trendizm.backend.ranking.application.dto.RankingResponse;
import com.trendizm.backend.ranking.application.dto.RankingSnapshot;
import com.trendizm.backend.ranking.application.dto.RankingTabs;
import com.trendizm.backend.ranking.application.dto.RankingTabs.PlatformTab;
import com.trendizm.backend.ranking.application.exception.RankingErrorCode;
import com.trendizm.backend.ranking.application.port.RankingService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RankingController.class)
@Import(SecurityConfig.class)
class RankingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RankingService rankingService;

    @Test
    @DisplayName("로그인 없이 platform과 category(둘 다 slug)로 최근 스냅샷을 조회한다")
    void 스냅샷_조회() throws Exception {
        RankingLinkResponse link = new RankingLinkResponse(501L, "두쫀쿠 만들기", "https://youtu.be/abc", null, "설명",
                LocalDateTime.of(2026, 11, 20, 18, 0));
        RankingResponse ranking = new RankingResponse(101L, 1, "두쫀쿠", null, List.of(link));
        given(rankingService.getLatestSnapshot("youtube", "food")).willReturn(new RankingSnapshot("youtube", 2L,
                LocalDateTime.of(2026, 11, 17, 9, 0), LocalDateTime.of(2026, 11, 24, 9, 0), List.of(ranking)));

        mockMvc.perform(get("/api/rankings").param("platform", "youtube").param("category", "food"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platform").value("youtube"))
                .andExpect(jsonPath("$.categoryId").value(2))
                .andExpect(jsonPath("$.startedAt").value("2026-11-17T09:00:00"))
                .andExpect(jsonPath("$.collectedAt").value("2026-11-24T09:00:00"))
                .andExpect(jsonPath("$.rankings[0].rank").value(1))
                .andExpect(jsonPath("$.rankings[0].keyword").value("두쫀쿠"))
                .andExpect(jsonPath("$.rankings[0].summary").isEmpty())
                .andExpect(jsonPath("$.rankings[0].links[0].url").value("https://youtu.be/abc"))
                .andExpect(jsonPath("$.rankings[0].links[0].publishedAt").value("2026-11-20T18:00:00"));
    }

    @Test
    @DisplayName("platform이나 category가 없으면 400 INVALID_INPUT")
    void 잘못된_파라미터() throws Exception {
        mockMvc.perform(get("/api/rankings").param("platform", "youtube"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        mockMvc.perform(get("/api/rankings").param("category", "food"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));

        verifyNoInteractions(rankingService);
    }

    @Test
    @DisplayName("서비스가 없는 플랫폼이라고 하면 404 RANKING_PLATFORM_NOT_FOUND")
    void 없는_플랫폼() throws Exception {
        given(rankingService.getLatestSnapshot("naver", "food"))
                .willThrow(new BusinessException(RankingErrorCode.RANKING_PLATFORM_NOT_FOUND));

        mockMvc.perform(get("/api/rankings").param("platform", "naver").param("category", "food"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RANKING_PLATFORM_NOT_FOUND"));
    }

    @Test
    @DisplayName("서비스가 없는 카테고리라고 하면 404 RANKING_CATEGORY_NOT_FOUND")
    void 없는_카테고리() throws Exception {
        given(rankingService.getLatestSnapshot("youtube", "nope"))
                .willThrow(new BusinessException(RankingErrorCode.RANKING_CATEGORY_NOT_FOUND));

        mockMvc.perform(get("/api/rankings").param("platform", "youtube").param("category", "nope"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RANKING_CATEGORY_NOT_FOUND"));
    }

    @Test
    @DisplayName("로그인 없이 플랫폼·카테고리 탭을 조회한다")
    void 탭_목록() throws Exception {
        given(rankingService.getTabs()).willReturn(new RankingTabs(
                List.of(new PlatformTab("youtube", "YouTube"), new PlatformTab("instagram", "Instagram")),
                List.of(new RankingCategoryResponse(1L, "sports", "운동/스포츠"),
                        new RankingCategoryResponse(2L, "food", "맛집/음식"))));

        mockMvc.perform(get("/api/rankings/tabs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platforms[0].slug").value("youtube"))
                .andExpect(jsonPath("$.platforms[0].label").value("YouTube"))
                .andExpect(jsonPath("$.platforms[1].slug").value("instagram"))
                .andExpect(jsonPath("$.categories[0].id").value(1))
                .andExpect(jsonPath("$.categories[0].slug").value("sports"))
                .andExpect(jsonPath("$.categories[1].title").value("맛집/음식"));
    }
}
