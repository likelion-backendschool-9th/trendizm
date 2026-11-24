package com.trendizm.backend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Scheduled 메서드를 실행한다. 예: 알림 SSE 연결 유지 신호(SseNotificationPusher.sendHeartbeat).
 * 탈퇴 30일 후 익명화(3.10) 같은 정기 작업도 @Scheduled만 붙이면 된다.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
