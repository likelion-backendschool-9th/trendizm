package com.trendizm.backend.rating.adapter.in.web;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.trendizm.backend.auth.adapter.out.security.SecurityConfig;
import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.rating.application.dto.RatingContentQuery;
import com.trendizm.backend.rating.application.dto.RatingContentResponse;
import com.trendizm.backend.rating.application.port.RatingService;
import com.trendizm.backend.rating.domain.entity.RatingContent;
import com.trendizm.backend.rating.domain.enums.RatingContentCategory;
import com.trendizm.backend.rating.domain.enums.RatingContentStatus;
import com.trendizm.backend.rating.domain.enums.RatingContentSubcategory;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RatingController.class)
@Import(SecurityConfig.class)
class RatingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RatingService ratingService;

    @Test
    @DisplayName("로그인하지 않아도 콘텐츠 상세를 볼 수 있다")
    void 상세() throws Exception {
        RatingContent content = RatingContent.create(RatingContentSubcategory.DRAMA, RatingContentStatus.ONGOING,
                "드라마", null, null, null, null, null);
        given(ratingService.getContent(1L)).willReturn(RatingContentResponse.from(content));

        mockMvc.perform(get("/api/ratings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subcategory.slug").value("drama"))
                .andExpect(jsonPath("$.status.slug").value("ongoing"))
                .andExpect(jsonPath("$.status.label").value("방영중"))
                .andExpect(jsonPath("$.stats.likeCount").value(0));
    }

    @Test
    @DisplayName("목록 필터는 slug 값으로 받는다")
    void 목록_필터() throws Exception {
        given(ratingService.getContents(new RatingContentQuery(RatingContentCategory.MEDIA,
                RatingContentSubcategory.DRAMA, RatingContentStatus.ENDED, 1, 10)))
                .willReturn(new PageResponse<>(List.of(), 1, 10, 0, 0, false));

        mockMvc.perform(get("/api/ratings").param("category", "media").param("subcategory", "drama")
                        .param("status", "ended").param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1));
    }

    @Test
    @DisplayName("없는 slug(대문자 code 포함)나 범위를 벗어난 size는 400")
    void 잘못된_값() throws Exception {
        mockMvc.perform(get("/api/ratings").param("category", "MEDIA"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RATING_INVALID_CATEGORY"));
        mockMvc.perform(get("/api/ratings").param("subcategory", "kdrama"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RATING_INVALID_CATEGORY"));
        mockMvc.perform(get("/api/ratings").param("status", "airing"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RATING_INVALID_STATUS"));
        mockMvc.perform(get("/api/ratings").param("size", "101")).andExpect(status().isBadRequest());
        verifyNoInteractions(ratingService);
    }
}
