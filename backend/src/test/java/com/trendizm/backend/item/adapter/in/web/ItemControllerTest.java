package com.trendizm.backend.item.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.trendizm.backend.item.application.dto.CreateItemCommand;
import com.trendizm.backend.item.application.dto.CreateItemQuestionCommand;
import com.trendizm.backend.item.application.dto.ItemCategoryInfo;
import com.trendizm.backend.item.application.dto.ItemAdjacentQuestions;
import com.trendizm.backend.item.application.dto.ItemQuestionQuery;
import com.trendizm.backend.item.application.dto.ItemQuestionResponse;
import com.trendizm.backend.item.application.port.ItemCommentSort;
import com.trendizm.backend.item.application.port.ItemService;
import com.trendizm.backend.item.domain.enums.ItemCategory;
import com.trendizm.backend.item.domain.enums.ItemSubcategory;
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

@WebMvcTest(ItemController.class)
@Import(SecurityConfig.class)
class ItemControllerTest {

    private static final CustomUserDetails LOGIN_USER = new CustomUserDetails(7L, "member@test.com", "encoded", List.of());

    private static final ItemQuestionResponse QUESTION = new ItemQuestionResponse(1L,
            ItemCategoryInfo.of(ItemCategory.FASHION), ItemCategoryInfo.of(ItemSubcategory.CLOTHING),
            "양말 추천해주세요", "내용", "질문자", false, 0, 0, LocalDateTime.now(), LocalDateTime.now(), 3);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @Test
    @DisplayName("로그인하지 않아도 질문 글 상세를 볼 수 있고, 보는 사람 id는 null로 넘긴다")
    void 비로그인_상세() throws Exception {
        given(itemService.getQuestion(eq(1L), isNull())).willReturn(QUESTION);

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subcategory.slug").value("clothing"))
                .andExpect(jsonPath("$.subcategory.label").value("의류"));
    }

    @Test
    @DisplayName("로그인하면 보는 사람 id를 넘긴다")
    void 로그인_상세() throws Exception {
        given(itemService.getQuestion(1L, 7L)).willReturn(QUESTION.withMine(true));

        mockMvc.perform(get("/api/items/1").with(user(LOGIN_USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mine").value(true));
    }

    @Test
    @DisplayName("로그인하지 않아도 댓글·추천 아이템 목록을 볼 수 있고, 댓글은 기본 10개씩이다")
    void 비로그인_목록() throws Exception {
        given(itemService.getComments(eq(1L), isNull(), eq(ItemCommentSort.LATEST), eq(0), eq(10)))
                .willReturn(new PageResponse<>(List.of(), 0, 10, 0, 0, false));
        given(itemService.getRecommendations(eq(1L), isNull())).willReturn(List.of());
        given(itemService.getAdjacentQuestions(1L)).willReturn(new ItemAdjacentQuestions(null, null));

        mockMvc.perform(get("/api/items/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(10));
        mockMvc.perform(get("/api/items/1/recommendations")).andExpect(status().isOk());
        mockMvc.perform(get("/api/items/1/adjacent")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("댓글 정렬은 slug(likes)로 받고, 없는 값이면 400 ITEM_INVALID_SORT")
    void 댓글_정렬() throws Exception {
        given(itemService.getComments(eq(1L), isNull(), eq(ItemCommentSort.LIKES), eq(0), eq(10)))
                .willReturn(new PageResponse<>(List.of(), 0, 10, 0, 0, false));

        mockMvc.perform(get("/api/items/1/comments").param("sort", "likes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(10));
        mockMvc.perform(get("/api/items/1/comments").param("sort", "popular"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ITEM_INVALID_SORT"));
    }

    @Test
    @DisplayName("목록 필터는 slug 값으로 받는다")
    void 목록_필터() throws Exception {
        given(itemService.getQuestions(new ItemQuestionQuery(ItemCategory.FASHION, ItemSubcategory.SHOES, 1, 10)))
                .willReturn(new PageResponse<>(List.of(), 1, 10, 0, 0, false));

        mockMvc.perform(get("/api/items")
                        .param("category", "fashion").param("subcategory", "shoes")
                        .param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1));
    }

    @Test
    @DisplayName("없는 카테고리 slug이거나 code 값(FASHION)으로 거르면 400 ITEM_INVALID_CATEGORY")
    void 잘못된_카테고리() throws Exception {
        mockMvc.perform(get("/api/items").param("category", "food"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ITEM_INVALID_CATEGORY"));
        mockMvc.perform(get("/api/items").param("subcategory", "CLOTHING"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ITEM_INVALID_CATEGORY"));
        verifyNoInteractions(itemService);
    }

    @Test
    @DisplayName("질문 글의 소분류가 없는 slug면 400 ITEM_INVALID_CATEGORY")
    void 잘못된_소분류() throws Exception {
        mockMvc.perform(post("/api/items").with(user(LOGIN_USER)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subcategory\":\"socks\",\"title\":\"제목\",\"content\":\"내용\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ITEM_INVALID_CATEGORY"));
        verifyNoInteractions(itemService);
    }

    @Test
    @DisplayName("로그인하지 않으면 글·댓글·공감·추천을 등록할 수 없다")
    void 비로그인_쓰기() throws Exception {
        mockMvc.perform(post("/api/items").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subcategory\":\"clothing\",\"title\":\"제목\",\"content\":\"내용\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/items/1/comments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"댓글\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/items/comments/1/likes")).andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/items/comments/1/likes")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/items/1/recommendations").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"양말\",\"description\":\"설명\"}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(itemService);
    }

    @Test
    @DisplayName("질문 글을 작성하면 201과 만든 글을 돌려주고, 앞뒤 공백은 지운다")
    void 질문_작성() throws Exception {
        given(itemService.createQuestion(any())).willReturn(QUESTION);

        mockMvc.perform(post("/api/items").with(user(LOGIN_USER)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subcategory\":\"clothing\",\"title\":\"  양말 추천해주세요 \",\"content\":\"내용\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
        verify(itemService).createQuestion(
                new CreateItemQuestionCommand(7L, ItemSubcategory.CLOTHING, "양말 추천해주세요", "내용"));
    }

    @Test
    @DisplayName("제목이 비어 있으면 400 INVALID_INPUT")
    void 빈_제목() throws Exception {
        mockMvc.perform(post("/api/items").with(user(LOGIN_USER)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subcategory\":\"clothing\",\"title\":\" \",\"content\":\"내용\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        verifyNoInteractions(itemService);
    }

    @Test
    @DisplayName("추천 아이템 링크가 http(s) 주소가 아니면 400 INVALID_INPUT")
    void 잘못된_링크() throws Exception {
        mockMvc.perform(post("/api/items/1/recommendations").with(user(LOGIN_USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"양말\",\"description\":\"설명\",\"linkUrl\":\"javascript:alert(1)\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        verifyNoInteractions(itemService);
    }

    @Test
    @DisplayName("댓글 공감·취소는 204이고 로그인한 회원 id를 넘긴다")
    void 공감() throws Exception {
        mockMvc.perform(post("/api/items/comments/3/likes").with(user(LOGIN_USER)))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/items/comments/3/likes").with(user(LOGIN_USER)))
                .andExpect(status().isNoContent());

        verify(itemService).likeComment(3L, 7L);
        verify(itemService).unlikeComment(3L, 7L);
    }

    @Test
    @DisplayName("이미지·링크가 비어 있거나 Https처럼 대문자로 시작해도 받는다")
    void 이미지_링크_허용() throws Exception {
        given(itemService.createRecommendation(any())).willReturn(null);

        mockMvc.perform(post("/api/items/1/recommendations").with(user(LOGIN_USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"양말\",\"description\":\"설명\",\"imageUrl\":\"\",\"linkUrl\":\"  \"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/items/1/recommendations").with(user(LOGIN_USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"양말\",\"description\":\"설명\","
                                + "\"imageUrl\":\"Https://img.test/a.jpg\",\"linkUrl\":\"HTTP://shop.test/a\"}"))
                .andExpect(status().isCreated());

        verify(itemService).createRecommendation(new CreateItemCommand(1L, 7L, "양말", null, null, List.of(), "설명",
                null, null));
        verify(itemService).createRecommendation(new CreateItemCommand(1L, 7L, "양말", null, null, List.of(), "설명",
                "Https://img.test/a.jpg", "HTTP://shop.test/a"));
    }

    @Test
    @DisplayName("빈 필터 값(?category=)은 보내지 않은 것과 같이 전체로 본다")
    void 빈_필터() throws Exception {
        given(itemService.getQuestions(new ItemQuestionQuery(null, null, 0, 20)))
                .willReturn(new PageResponse<>(List.of(), 0, 20, 0, 0, false));

        mockMvc.perform(get("/api/items").param("category", "").param("subcategory", " "))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("잠금 해제는 로그인한 회원 id를 넘기고, 로그인하지 않으면 401")
    void 잠금_해제() throws Exception {
        given(itemService.unlockRecommendation(5L, 7L)).willReturn(null);

        mockMvc.perform(post("/api/items/recommendations/5/unlock").with(user(LOGIN_USER)))
                .andExpect(status().isOk());
        verify(itemService).unlockRecommendation(5L, 7L);

        mockMvc.perform(post("/api/items/recommendations/5/unlock")).andExpect(status().isUnauthorized());
    }
}
