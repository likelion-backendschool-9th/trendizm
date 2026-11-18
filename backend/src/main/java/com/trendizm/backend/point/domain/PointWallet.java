package com.trendizm.backend.point.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * point_wallets 테이블. 회원 1명당 지갑 1개이고, 처음 포인트가 오갈 때 만든다.
 * 잔액을 바꿀 때는 서비스에서 지갑 행을 잠그고(SELECT ... FOR UPDATE) 바꾼다.
 * TODO(point): #18(BaseTimeEntity)이 develop에 머지되면 created_at·updated_at을 BaseTimeEntity 상속으로 바꾼다.
 */
@Entity
@Table(name = "point_wallets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    /** 현재 잔액. 0보다 작아질 수 없다 */
    @Column(nullable = false)
    private int balance;

    /** 행 잠금과 별개로 남겨 두는 버전 (ERD의 version 컬럼) */
    @Version
    @Column(nullable = false)
    private long version;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private PointWallet(Long userId) {
        this.userId = userId;
        this.balance = 0;
    }

    /** 잔액 0인 새 지갑 */
    public static PointWallet create(Long userId) {
        return new PointWallet(userId);
    }

    public boolean canDeduct(int amount) {
        return amount > 0 && balance >= amount;
    }

    /** 잔액이 부족하면 IllegalStateException. 서비스에서 canDeduct로 먼저 확인한다 */
    public void deduct(int amount) {
        requirePositive(amount);
        if (balance < amount) {
            throw new IllegalStateException("잔액이 부족합니다. balance=" + balance + ", amount=" + amount);
        }
        balance -= amount;
    }

    public void earn(int amount) {
        requirePositive(amount);
        balance = Math.addExact(balance, amount);
    }

    private static void requirePositive(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("포인트 금액은 0보다 커야 합니다. amount=" + amount);
        }
    }
}
