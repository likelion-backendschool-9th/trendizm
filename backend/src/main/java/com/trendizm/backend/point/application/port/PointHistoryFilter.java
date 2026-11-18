package com.trendizm.backend.point.application.port;

/**
 * 포인트 내역 화면의 탭. 어떤 PointType을 포함하는지는 PointServiceImpl에서 정한다.
 * ALL 전체 / EARN 적립(열람 적립·활동 보상) / USE 사용(열람 차감) / CHARGE 충전(충전·환불)
 */
public enum PointHistoryFilter {
    ALL,
    EARN,
    USE,
    CHARGE
}
