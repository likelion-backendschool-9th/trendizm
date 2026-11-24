package com.trendizm.backend.lookup.adapter.in.web;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.trendizm.backend.auth.adapter.out.security.CustomUserDetails;
import com.trendizm.backend.auth.adapter.out.security.SecurityConfig;
import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.lookup.application.dto.CreateMemeCommentCommand;
import com.trendizm.backend.lookup.application.dto.MemeCommentResponse;
import com.trendizm.backend.lookup.application.dto.UpdateMemeCommentCommand;
import com.trendizm.backend.lookup.application.port.MemeCommentService;
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

@WebMvcTest(MemeCommentController.class)
@Import(SecurityConfig.class)
class MemeCommentControllerTest {

    private static final CustomUserDetails LOGIN_USER = new CustomUserDetails(7L, "member@test.com", "encoded", List.of());
    private static final String COMMENTS = "/api/lookup/interpretations/{interpretationId}/comments";
    private static final String COMMENT = "/api/lookup/comments/{commentId}";
    private static final String LIKES = "/api/lookup/comments/{commentId}/likes";
    private static final MemeCommentResponse COMMENT_RESPONSE = new MemeCommentResponse(
            5L, null, 7L, "밈장인", "댓글", 0, false, false, LocalDateTime.now(), LocalDateTime.now(), List.of());

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemeCommentService commentService;

    @Test
    @DisplayName("댓글 목록은 로그인 없이 볼 수 있고(viewerId null), 로그인하면 viewerId를 넘긴다")
    void 댓글_목록() throws Exception {
        PageResponse<MemeCommentResponse> page = new PageResponse<>(List.of(COMMENT_RESPONSE), 0, 20, 1, 1, false);
        given(commentService.getComments(1L, null, 0, 20)).willReturn(page);
        given(commentService.getComments(1L, 7L, 0, 20)).willReturn(page);

        mockMvc.perform(get(COMMENTS, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].content").value("댓글"));
        mockMvc.perform(get(COMMENTS, 1L).with(user(LOGIN_USER)))
                .andExpect(status().isOk());
        verify(commentService).getComments(1L, 7L, 0, 20);
    }

    @Test
    @DisplayName("로그인하면 댓글·대댓글을 쓸 수 있고, 내용 앞뒤 공백은 지운다")
    void 댓글_작성() throws Exception {
        given(commentService.createComment(new CreateMemeCommentCommand(1L, 3L, 7L, "답글"))).willReturn(COMMENT_RESPONSE);

        mockMvc.perform(post(COMMENTS, 1L).with(user(LOGIN_USER)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"parentId\":3,\"content\":\"  답글  \"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    @DisplayName("내용이 비었거나 500자를 넘으면 400 INVALID_INPUT")
    void 잘못된_내용() throws Exception {
        for (String body : List.of("{\"content\":\" \"}", "{\"content\":\"" + "가".repeat(501) + "\"}")) {
            mockMvc.perform(post(COMMENTS, 1L).with(user(LOGIN_USER)).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
            mockMvc.perform(patch(COMMENT, 5L).with(user(LOGIN_USER)).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        }
        verifyNoInteractions(commentService);
    }

    @Test
    @DisplayName("보이지 않는 글자(한글 채움 문자·폭 없는 공백 등)만 있거나 NULL 같은 제어 문자가 있으면 400 INVALID_INPUT")
    void 보이지_않는_내용과_제어_문자() throws Exception {
        for (String content : List.of("\\u3164", "\\u200B", "\\u00A0\\u3000", "\\u2800", "내용\\u0000", "\\u0007경고", "a\\u001Bb")) {
            String body = "{\"content\":\"" + content + "\"}";
            mockMvc.perform(post(COMMENTS, 1L).with(user(LOGIN_USER)).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
            mockMvc.perform(patch(COMMENT, 5L).with(user(LOGIN_USER)).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        }
        verifyNoInteractions(commentService);
    }

    @Test
    @DisplayName("줄바꿈·탭은 내용에 쓸 수 있다")
    void 줄바꿈_내용() throws Exception {
        given(commentService.createComment(new CreateMemeCommentCommand(1L, null, 7L, "첫 줄\n\t둘째 줄")))
                .willReturn(COMMENT_RESPONSE);

        mockMvc.perform(post(COMMENTS, 1L).with(user(LOGIN_USER)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"첫 줄\\n\\t둘째 줄\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("로그인하면 댓글을 고치고(200) 지우고(204) 좋아요·취소(204)할 수 있다")
    void 수정_삭제_좋아요() throws Exception {
        given(commentService.updateComment(new UpdateMemeCommentCommand(5L, 7L, "고친 내용"))).willReturn(COMMENT_RESPONSE);

        mockMvc.perform(patch(COMMENT, 5L).with(user(LOGIN_USER)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"고친 내용\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete(COMMENT, 5L).with(user(LOGIN_USER))).andExpect(status().isNoContent());
        mockMvc.perform(post(LIKES, 5L).with(user(LOGIN_USER))).andExpect(status().isNoContent());
        mockMvc.perform(delete(LIKES, 5L).with(user(LOGIN_USER))).andExpect(status().isNoContent());

        verify(commentService).deleteComment(5L, 7L);
        verify(commentService).likeComment(5L, 7L);
        verify(commentService).unlikeComment(5L, 7L);
    }

    @Test
    @DisplayName("로그인하지 않으면 댓글 작성·수정·삭제·좋아요를 할 수 없다 (401)")
    void 비로그인() throws Exception {
        mockMvc.perform(post(COMMENTS, 1L).contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"댓글\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(patch(COMMENT, 5L).contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"댓글\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete(COMMENT, 5L)).andExpect(status().isUnauthorized());
        mockMvc.perform(post(LIKES, 5L)).andExpect(status().isUnauthorized());
        mockMvc.perform(delete(LIKES, 5L)).andExpect(status().isUnauthorized());
        verifyNoInteractions(commentService);
    }
}
