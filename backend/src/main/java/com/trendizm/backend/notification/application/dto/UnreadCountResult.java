package com.trendizm.backend.notification.application.dto;

/**
 * 안 읽은 알림 수. 묶인 알림("동의 5개")도 1개로 센다.
 * GET /api/notifications/unread-count 응답과 SSE "unread-count" 이벤트 본문에 같이 쓴다. 예: { "count": 3 }
 */
public record UnreadCountResult(long count) {
}
