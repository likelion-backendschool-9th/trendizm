package com.trendizm.backend.notification.adapter.in.web;

import com.trendizm.backend.notification.adapter.in.web.dto.NotificationRequest;
import com.trendizm.backend.notification.application.dto.NotificationResponse;
import com.trendizm.backend.notification.application.port.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 모든 요청에 로그인이 필요하다 (auth/adapter/out/security/SecurityConfig) */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public Page<NotificationResponse> getNotifications(Pageable pageable) {
        return notificationService.getNotifications(pageable);
    }

    @GetMapping("/{notificationId}")
    public NotificationResponse getNotification(@PathVariable Long notificationId) {
        return notificationService.getNotification(notificationId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long createNotification(@Valid @RequestBody NotificationRequest request) {
        return notificationService.createNotification();
    }

    @PutMapping("/{notificationId}")
    public void updateNotification(@PathVariable Long notificationId, @Valid @RequestBody NotificationRequest request) {
        notificationService.updateNotification(notificationId);
    }

    @DeleteMapping("/{notificationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
    }
}
