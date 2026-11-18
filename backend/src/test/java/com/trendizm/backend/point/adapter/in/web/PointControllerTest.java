package com.trendizm.backend.point.adapter.in.web;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.trendizm.backend.auth.adapter.out.security.CustomUserDetails;
import com.trendizm.backend.auth.adapter.out.security.SecurityConfig;
import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.point.application.dto.PointBalanceResponse;
import com.trendizm.backend.point.application.dto.PointHistoryResponse;
import com.trendizm.backend.point.application.port.PointHistoryFilter;
import com.trendizm.backend.point.application.port.PointService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PointController.class)
@Import(SecurityConfig.class)
class PointControllerTest {

    private static final CustomUserDetails LOGIN_USER = new CustomUserDetails(7L, "member@test.com", "encoded", List.of());

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PointService pointService;

    @Test
    @DisplayName("로그인한 회원의 보유 포인트를 돌려준다")
    void 보유_포인트() throws Exception {
        given(pointService.getBalance(7L)).willReturn(new PointBalanceResponse(1200));

        mockMvc.perform(get("/api/points/me").with(user(LOGIN_USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1200));
    }

    @Test
    @DisplayName("포인트 내역은 type 필터와 페이지 값을 서비스에 넘긴다")
    void 포인트_내역() throws Exception {
        PointHistoryResponse history = new PointHistoryResponse(3L, "UNLOCK", -100, 900, LocalDateTime.now());
        given(pointService.getHistories(eq(7L), eq(PointHistoryFilter.USE), eq(1), eq(10)))
                .willReturn(new PageResponse<>(List.of(history), 1, 10, 11, 2, false));

        mockMvc.perform(get("/api/points").param("type", "USE").param("page", "1").param("size", "10")
                        .with(user(LOGIN_USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("UNLOCK"))
                .andExpect(jsonPath("$.content[0].amount").value(-100))
                .andExpect(jsonPath("$.page").value(1));
    }

    @Test
    @DisplayName("size가 100을 넘으면 400 INVALID_INPUT")
    void 잘못된_페이지_크기() throws Exception {
        mockMvc.perform(get("/api/points").param("size", "101").with(user(LOGIN_USER)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        verifyNoInteractions(pointService);
    }

    @Test
    @DisplayName("로그인하지 않으면 포인트를 조회할 수 없다")
    void 비로그인() throws Exception {
        mockMvc.perform(get("/api/points/me"))
                .andExpect(status().is4xxClientError());
        verifyNoInteractions(pointService);
    }
}
