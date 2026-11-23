package com.trendizm.backend.item.domain.entity;

import com.trendizm.backend.global.entity.BaseCreatedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * item_unlocks 테이블. 추천 아이템 열람 기록. 한 회원은 한 아이템을 한 번만 열람(결제)한다.
 * 열람자 차감(UNLOCK)과 등록자 적립(EARN)은 같은 트랜잭션에서 포인트 도메인이 처리하고, 포인트 내역에 이 id를 남긴다.
 * 서비스에서는 동시에 열람해도 한 줄만 생기도록 ItemUnlockRepository.insertIfAbsent로 만든다 (create는 테스트·직접 생성용).
 */
@Entity
@Table(name = "item_unlocks", uniqueConstraints = @UniqueConstraint(columnNames = {"item_id", "user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemUnlock extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long itemId;

    /** 열람한 회원 */
    @Column(nullable = false)
    private Long userId;

    /** 열람 당시 가격 */
    @Column(nullable = false)
    private int price;

    private ItemUnlock(Long itemId, Long userId, int price) {
        this.itemId = Objects.requireNonNull(itemId, "itemId");
        this.userId = Objects.requireNonNull(userId, "userId");
        if (price <= 0) {
            throw new IllegalArgumentException("열람 가격은 0보다 커야 합니다. price=" + price);
        }
        this.price = price;
    }

    public static ItemUnlock create(Long itemId, Long userId, int price) {
        return new ItemUnlock(itemId, userId, price);
    }
}
