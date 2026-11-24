package com.trendizm.backend.notification.adapter.in.web;

import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.notification.application.dto.NotificationResponse;
import com.trendizm.backend.notification.application.dto.UnreadCountResult;
import com.trendizm.backend.notification.application.port.NotificationService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** 모든 요청에 로그인이 필요하다 (auth/adapter/out/security/SecurityConfig). 본인 알림만 다룬다 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** 내 알림 목록 (최신순) */
    @GetMapping
    public PageResponse<NotificationResponse> getNotifications(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "size는 1 이상이어야 합니다.")
            @Max(value = 50, message = "size는 50 이하여야 합니다.") int size) {
        return notificationService.getNotifications(userId, page, size);
    }

    /** 안 읽은 알림 수. 헤더 종 아이콘의 숫자 */
    @GetMapping("/unread-count")
    public UnreadCountResult getUnreadCount(@AuthenticationPrincipal(expression = "id") Long userId) {
        return new UnreadCountResult(notificationService.getUnreadCount(userId));
    }

    /** 읽음 처리. 이미 읽은 알림이어도 성공이다 */
    @PatchMapping("/{notificationId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAsRead(@AuthenticationPrincipal(expression = "id") Long userId,
                           @PathVariable Long notificationId) {
        notificationService.markAsRead(userId, notificationId);
    }

    /**
     * 실시간 알림 (SSE). 연결되자마자, 그리고 안 읽은 수가 바뀔 때마다 "unread-count" 이벤트로 { "count": n }을 보낸다.
     * - X-Accel-Buffering: no → nginx가 응답을 모아 두지 않고 바로 흘려보낸다
     * - Cache-Control: no-transform → Next(BFF)가 응답을 압축하느라 모아 두지 않는다
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> stream(@AuthenticationPrincipal(expression = "id") Long userId) {
        SseEmitter emitter = notificationService.subscribe(userId);
        return ResponseEntity.ok()
                .header("X-Accel-Buffering", "no")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-transform")
                .body(emitter);
    }
}
