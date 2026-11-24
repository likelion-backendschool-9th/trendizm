package com.trendizm.backend.point.domain.entity;

import com.trendizm.backend.global.entity.BaseCreatedEntity;
import com.trendizm.backend.point.domain.enums.PointType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * point_histories 테이블. 잔액이 바뀔 때마다 한 줄씩 남기고 수정하지 않는다.
 * 참조 id는 종류마다 하나만 채운다: CHARGE·REFUND → chargeId, UNLOCK·EARN → itemUnlockId, REWARD → ratingId.
 * 다른 도메인 테이블은 id 값만 저장한다. created_at은 BaseCreatedEntity가 채운다.
 * item_unlock_id·rating_id 인덱스는 결제·보상 전에 "이미 처리했나"를 확인하는 조회(existsBy...AndType)가
 * 내역 전체를 읽지 않게 한다. 한 값에 해당하는 행이 최대 2개(UNLOCK·EARN)라 type은 넣지 않는다.
 */
@Entity
@Table(name = "point_histories", indexes = {
        @Index(columnList = "user_id, created_at"),
        @Index(columnList = "item_unlock_id"),
        @Index(columnList = "rating_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistory extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    /** DB에는 enum 이름을 문자열로 저장한다 (ERD: varchar(30)) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PointType type;

    /** 적립은 +, 차감은 - */
    @Column(nullable = false)
    private int amount;

    /** 처리 후 잔액 */
    @Column(nullable = false)
    private int balanceAfter;

    private Long chargeId;

    private Long itemUnlockId;

    private Long ratingId;

    private PointHistory(Long userId, PointType type, int amount, int balanceAfter,
                         Long chargeId, Long itemUnlockId, Long ratingId) {
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.chargeId = chargeId;
        this.itemUnlockId = itemUnlockId;
        this.ratingId = ratingId;
    }

    /**
     * 지갑의 현재 잔액으로 내역을 만든다. 지갑 잔액을 바꾼 뒤에 호출한다.
     * referenceId는 type에 맞는 참조 id다 (CHARGE·REFUND: 충전 id, UNLOCK·EARN: 열람 id, REWARD: 후기 id).
     */
    public static PointHistory create(PointWallet wallet, PointType type, int amount, Long referenceId) {
        if (referenceId == null) {
            throw new IllegalArgumentException(type + " 내역에는 참조 id가 필요합니다.");
        }
        int signedAmount = switch (type) {
            case CHARGE, EARN, REWARD -> amount;
            case UNLOCK, REFUND -> -amount;
        };
        return new PointHistory(
                wallet.getUserId(), type, signedAmount, wallet.getBalance(),
                type == PointType.CHARGE || type == PointType.REFUND ? referenceId : null,
                type == PointType.UNLOCK || type == PointType.EARN ? referenceId : null,
                type == PointType.REWARD ? referenceId : null);
    }
}
