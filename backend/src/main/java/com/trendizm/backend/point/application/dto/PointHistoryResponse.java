package com.trendizm.backend.point.application.dto;

import com.trendizm.backend.point.domain.PointHistory;
import java.time.LocalDateTime;

/**
 * 포인트 내역 한 줄. type은 PointType 이름(CHARGE, UNLOCK, EARN, REWARD, REFUND)이다.
 * 예: { "id": 3, "type": "UNLOCK", "amount": -100, "balanceAfter": 900, "createdAt": "..." }
 */
public record PointHistoryResponse(Long id, String type, int amount, int balanceAfter, LocalDateTime createdAt) {

    public static PointHistoryResponse from(PointHistory history) {
        return new PointHistoryResponse(
                history.getId(), history.getType().name(), history.getAmount(),
                history.getBalanceAfter(), history.getCreatedAt());
    }
}
