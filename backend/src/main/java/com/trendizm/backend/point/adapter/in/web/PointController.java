package com.trendizm.backend.point.adapter.in.web;

import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.point.application.dto.PointBalanceResponse;
import com.trendizm.backend.point.application.dto.PointHistoryResponse;
import com.trendizm.backend.point.application.port.PointHistoryFilter;
import com.trendizm.backend.point.application.port.PointService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 로그인 필요 (auth/adapter/out/security/SecurityConfig에서 /api/points/**는 공개 목록에 없다) */
@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    /** 보유 포인트. 예: { "balance": 1200 } */
    @GetMapping("/me")
    public PointBalanceResponse getMyBalance(@AuthenticationPrincipal(expression = "id") Long userId) {
        return pointService.getBalance(userId);
    }

    /** 포인트 내역 (최신순). type: ALL 전체 / EARN 적립 / USE 사용 / CHARGE 충전 */
    @GetMapping
    public PageResponse<PointHistoryResponse> getMyHistories(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @RequestParam(defaultValue = "ALL") PointHistoryFilter type,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "size는 1 이상이어야 합니다.")
            @Max(value = 100, message = "size는 100 이하여야 합니다.") int size) {
        return pointService.getHistories(userId, type, page, size);
    }
}
