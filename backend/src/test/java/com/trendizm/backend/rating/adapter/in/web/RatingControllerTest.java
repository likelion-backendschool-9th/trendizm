package com.trendizm.backend.rating.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.trendizm.backend.auth.adapter.out.security.CustomUserDetails;
import com.trendizm.backend.auth.adapter.out.security.SecurityConfig;
import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.rating.application.dto.RatingContentQuery;
import com.trendizm.backend.rating.application.dto.RatingContentResponse;
import com.trendizm.backend.rating.application.dto.RatingReviewQuery;
import com.trendizm.backend.rating.application.dto.RatingReviewResponse;
import com.trendizm.backend.rating.application.dto.RatingReviewSaveResult;
import com.trendizm.backend.rating.application.dto.SaveRatingReviewCommand;
import com.trendizm.backend.rating.application.port.RatingService;
import com.trendizm.backend.rating.application.port.ReviewSort;
import com.trendizm.backend.rating.domain.entity.Rating;
import com.trendizm.backend.rating.domain.entity.RatingContent;
import com.trendizm.backend.rating.domain.enums.RatingContentCategory;
import com.trendizm.backend.rating.domain.enums.RatingContentStatus;
import com.trendizm.backend.rating.domain.enums.RatingContentSubcategory;
import com.trendizm.backend.rating.domain.enums.Sentiment;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RatingController.class)
@Import(SecurityConfig.class)
class RatingControllerTest {

    private static final CustomUserDetails LOGIN_USER = new CustomUserDetails(7L, "member@test.com", "encoded", List.of());

    private static final String REVIEW_JSON =
            "{\"sentiment\":\"like\",\"score\":5,\"title\":\" 최고 \",\"body\":\"재밌어요\",\"progress\":8}";

    private static RatingReviewResponse review() {
        return RatingReviewResponse.from(Rating.create(1L, 7L, Sentiment.LIKE, 5, "최고", "재밌어요", 8));
    }

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

    @Test
    @DisplayName("빈 필터 값(?category=)은 보내지 않은 것과 같이 전체로 본다")
    void 빈_필터() throws Exception {
        given(ratingService.getContents(new RatingContentQuery(null, null, null, 0, 20)))
                .willReturn(new PageResponse<>(List.of(), 0, 20, 0, 0, false));

        mockMvc.perform(get("/api/ratings").param("category", "").param("subcategory", " ").param("status", ""))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그인하지 않아도 후기 목록을 볼 수 있고, slug로 받은 정렬·필터 값을 넘긴다")
    void 후기_목록() throws Exception {
        given(ratingService.getReviews(new RatingReviewQuery(1L, Sentiment.DISLIKE, ReviewSort.SCORE_LOW, null, 0, 20)))
                .willReturn(new PageResponse<>(List.of(review()), 0, 20, 1, 1, false));

        mockMvc.perform(get("/api/ratings/1/reviews").param("sentiment", "dislike").param("sort", "score-low"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].sentiment.label").value("좋아요"));
    }

    @Test
    @DisplayName("로그인하지 않으면 후기를 쓸 수 없다")
    void 비로그인_작성() throws Exception {
        mockMvc.perform(post("/api/ratings/1/reviews").contentType(MediaType.APPLICATION_JSON).content(REVIEW_JSON))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(ratingService);
    }

    @Test
    @DisplayName("처음 쓰면 201과 적립 포인트를, 수정하면 200을 돌려준다")
    void 후기_작성() throws Exception {
        given(ratingService.saveReview(any()))
                .willReturn(new RatingReviewSaveResult(review(), true, 10))
                .willReturn(new RatingReviewSaveResult(review(), false, 0));

        mockMvc.perform(post("/api/ratings/1/reviews").with(user(LOGIN_USER))
                        .contentType(MediaType.APPLICATION_JSON).content(REVIEW_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rewardPoint").value(10))
                .andExpect(jsonPath("$.review.score").value(5));
        mockMvc.perform(post("/api/ratings/1/reviews").with(user(LOGIN_USER))
                        .contentType(MediaType.APPLICATION_JSON).content(REVIEW_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.created").value(false));

        verify(ratingService, org.mockito.Mockito.times(2)).saveReview(
                new SaveRatingReviewCommand(1L, 7L, Sentiment.LIKE, 5, "최고", "재밌어요", 8));
    }

    @Test
    @DisplayName("별점이 1~5가 아니거나 제목이 비어 있으면 400 INVALID_INPUT")
    void 잘못된_후기() throws Exception {
        mockMvc.perform(post("/api/ratings/1/reviews").with(user(LOGIN_USER)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sentiment\":\"like\",\"score\":6,\"title\":\"제목\",\"body\":\"본문\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        mockMvc.perform(post("/api/ratings/1/reviews").with(user(LOGIN_USER)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sentiment\":\"like\",\"score\":3,\"title\":\" \",\"body\":\"본문\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(ratingService);
    }

    @Test
    @DisplayName("없는 호/불호·정렬 값이면 400")
    void 잘못된_후기_필터() throws Exception {
        mockMvc.perform(get("/api/ratings/1/reviews").param("sentiment", "LIKE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RATING_INVALID_SENTIMENT"));
        mockMvc.perform(get("/api/ratings/1/reviews").param("sort", "best"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RATING_INVALID_SORT"));
        mockMvc.perform(post("/api/ratings/1/reviews").with(user(LOGIN_USER)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sentiment\":\"love\",\"score\":5,\"title\":\"제목\",\"body\":\"본문\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RATING_INVALID_SENTIMENT"));
        verifyNoInteractions(ratingService);
    }

    @Test
    @DisplayName("후기 공감·취소는 204이고 로그인한 회원 id를 넘긴다")
    void 후기_공감() throws Exception {
        mockMvc.perform(post("/api/ratings/reviews/3/likes").with(user(LOGIN_USER))).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/ratings/reviews/3/likes").with(user(LOGIN_USER))).andExpect(status().isNoContent());

        verify(ratingService).likeReview(3L, 7L);
        verify(ratingService).unlikeReview(3L, 7L);
    }

    @Test
    @DisplayName("로그인하지 않으면 후기에 공감할 수 없다")
    void 비로그인_공감() throws Exception {
        mockMvc.perform(post("/api/ratings/reviews/3/likes")).andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/ratings/reviews/3/likes")).andExpect(status().isUnauthorized());
        verifyNoInteractions(ratingService);
    }

    @Test
    @DisplayName("공감 많은 순 정렬 값을 받는다")
    void 공감순_정렬() throws Exception {
        given(ratingService.getReviews(new RatingReviewQuery(1L, null, ReviewSort.LIKES, null, 0, 20)))
                .willReturn(new PageResponse<>(List.of(review()), 0, 20, 1, 1, false));

        mockMvc.perform(get("/api/ratings/1/reviews").param("sort", "likes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].likeCount").value(0));
    }
}
