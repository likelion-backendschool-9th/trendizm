package com.trendizm.backend.notification.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.trendizm.backend.auth.adapter.out.security.CustomUserDetails;
import com.trendizm.backend.auth.adapter.out.security.SecurityConfig;
import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.notification.adapter.out.sse.SseNotificationPusher;
import com.trendizm.backend.notification.application.dto.NotificationResponse;
import com.trendizm.backend.notification.application.exception.NotificationErrorCode;
import com.trendizm.backend.notification.application.port.NotificationService;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@WebMvcTest(NotificationController.class)
@Import(SecurityConfig.class)
class NotificationControllerTest {

    private static final CustomUserDetails LOGIN_USER = new CustomUserDetails(7L, "member@test.com", "encoded", List.of());

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    @DisplayName("로그인하지 않으면 알림 API는 모두 401")
    void 비로그인() throws Exception {
        mockMvc.perform(get("/api/notifications")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/notifications/unread-count")).andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/api/notifications/1/read")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/notifications/stream")).andExpect(status().isUnauthorized());
        verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("내 알림 목록을 로그인한 회원 id로 조회한다")
    void 목록() throws Exception {
        NotificationResponse notification = new NotificationResponse(3L, "meme-vote", 1L, 2L, "tmi", 4,
                "‘tmi’에 쓴 해석에 동의 4개가 달렸어요.", false, LocalDateTime.of(2026, 11, 24, 12, 0));
        given(notificationService.getNotifications(7L, 0, 10))
                .willReturn(new PageResponse<>(List.of(notification), 0, 10, 1, 1, false));

        mockMvc.perform(get("/api/notifications").param("size", "10").with(user(LOGIN_USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("meme-vote"))
                .andExpect(jsonPath("$.content[0].count").value(4))
                .andExpect(jsonPath("$.content[0].read").value(false));
    }

    @Test
    @DisplayName("size가 50을 넘으면 400")
    void 목록_size_제한() throws Exception {
        mockMvc.perform(get("/api/notifications").param("size", "51").with(user(LOGIN_USER)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("안 읽은 수")
    void 안_읽은_수() throws Exception {
        given(notificationService.getUnreadCount(7L)).willReturn(3L);

        mockMvc.perform(get("/api/notifications/unread-count").with(user(LOGIN_USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(3));
    }

    @Test
    @DisplayName("읽음 처리는 204, 없거나 남의 알림이면 404")
    void 읽음_처리() throws Exception {
        mockMvc.perform(patch("/api/notifications/3/read").with(user(LOGIN_USER)))
                .andExpect(status().isNoContent());
        verify(notificationService).markAsRead(7L, 3L);

        willThrow(new BusinessException(NotificationErrorCode.NOTIFICATION_NOT_FOUND))
                .given(notificationService).markAsRead(7L, 4L);
        mockMvc.perform(patch("/api/notifications/4/read").with(user(LOGIN_USER)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOTIFICATION_NOT_FOUND"));
    }

    @Test
    @DisplayName("SSE는 연결하자마자 안 읽은 수를 보내고, 중간 서버가 모아 두지 않게 하는 헤더를 붙인다")
    void SSE_연결() throws Exception {
        SseNotificationPusher pusher = new SseNotificationPusher();
        SseEmitter emitter = pusher.subscribe(7L, 2L);
        given(notificationService.subscribe(7L)).willReturn(emitter);

        MvcResult result = mockMvc.perform(get("/api/notifications/stream")
                        .accept(MediaType.TEXT_EVENT_STREAM).with(user(LOGIN_USER)))
                .andExpect(request().asyncStarted())
                .andExpect(header().string("X-Accel-Buffering", "no"))
                .andExpect(header().string("Cache-Control", "no-cache, no-transform"))
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .contains("event:unread-count")
                .contains("data:{\"count\":2}");

        // 새 알림이 생기면 같은 연결로 바뀐 수가 간다
        pusher.pushUnreadCount(7L, () -> 3L);
        assertThat(result.getResponse().getContentAsString()).contains("data:{\"count\":3}");

        // 연결이 끝날 때의 내부 재처리(ASYNC dispatch)는 JWT 없이 오지만 401·403이 나지 않아야 한다
        emitter.complete();
        mockMvc.perform(asyncDispatch(result)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("연결 수 제한으로 밀려난 연결에는 닫기 전에 evicted를 보내 화면이 다시 연결하지 않게 한다")
    void SSE_밀려난_연결() throws Exception {
        SseNotificationPusher pusher = new SseNotificationPusher();
        given(notificationService.subscribe(7L)).willReturn(pusher.subscribe(7L, 0L));
        MvcResult oldest = mockMvc.perform(get("/api/notifications/stream")
                        .accept(MediaType.TEXT_EVENT_STREAM).with(user(LOGIN_USER)))
                .andExpect(request().asyncStarted())
                .andReturn();

        for (int i = 0; i < 5; i++) {
            pusher.subscribe(7L, 0L);
        }

        assertThat(oldest.getResponse().getContentAsString()).contains("event:evicted");
        mockMvc.perform(asyncDispatch(oldest)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("서버가 꺼지기 시작하면 열린 SSE 연결을 모두 닫는다")
    void SSE_서버_종료() throws Exception {
        SseNotificationPusher pusher = new SseNotificationPusher();
        given(notificationService.subscribe(7L)).willReturn(pusher.subscribe(7L, 0L));
        MvcResult result = mockMvc.perform(get("/api/notifications/stream")
                        .accept(MediaType.TEXT_EVENT_STREAM).with(user(LOGIN_USER)))
                .andExpect(request().asyncStarted())
                .andReturn();

        pusher.closeAll();

        // 연결이 끝나서 요청이 바로 마무리된다(끝나지 않았으면 asyncDispatch가 결과를 기다리며 멈춘다)
        mockMvc.perform(asyncDispatch(result)).andExpect(status().isOk());
        assertThat(result.getResponse().getContentAsString()).doesNotContain("event:evicted");
    }
}
