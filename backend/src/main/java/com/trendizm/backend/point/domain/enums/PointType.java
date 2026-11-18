package com.trendizm.backend.point.domain.enums;

/** 포인트 변동 종류. point_histories.type에 이름 그대로 저장한다 */
public enum PointType {
    /** PG 충전 (+) */
    CHARGE,
    /** 추천 아이템 열람 차감 (-) */
    UNLOCK,
    /** 내 추천 아이템이 열람되어 받은 적립 (+) */
    EARN,
    /** 후기 작성 등 활동 보상 (+) */
    REWARD,
    /** 충전 환불 (-) */
    REFUND
}
