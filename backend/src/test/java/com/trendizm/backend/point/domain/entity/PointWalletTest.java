package com.trendizm.backend.point.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PointWalletTest {

    @Test
    @DisplayName("새 지갑의 잔액은 0이다")
    void 새_지갑() {
        PointWallet wallet = PointWallet.create(1L);

        assertThat(wallet.getUserId()).isEqualTo(1L);
        assertThat(wallet.getBalance()).isZero();
    }

    @Test
    @DisplayName("적립하면 잔액이 늘고, 차감하면 줄어든다")
    void 적립과_차감() {
        PointWallet wallet = PointWallet.create(1L);

        wallet.earn(300);
        wallet.deduct(100);

        assertThat(wallet.getBalance()).isEqualTo(200);
    }

    @Test
    @DisplayName("잔액보다 많이 차감하면 예외가 나고 잔액은 그대로다")
    void 잔액_부족() {
        PointWallet wallet = PointWallet.create(1L);
        wallet.earn(50);

        assertThat(wallet.canDeduct(100)).isFalse();
        assertThatThrownBy(() -> wallet.deduct(100)).isInstanceOf(IllegalStateException.class);
        assertThat(wallet.getBalance()).isEqualTo(50);
    }

    @Test
    @DisplayName("잔액과 같은 금액은 차감할 수 있다")
    void 잔액_전부_차감() {
        PointWallet wallet = PointWallet.create(1L);
        wallet.earn(100);

        assertThat(wallet.canDeduct(100)).isTrue();
        wallet.deduct(100);

        assertThat(wallet.getBalance()).isZero();
    }

    @Test
    @DisplayName("0 이하 금액은 적립·차감할 수 없다")
    void 잘못된_금액() {
        PointWallet wallet = PointWallet.create(1L);

        assertThat(wallet.canDeduct(0)).isFalse();
        assertThatThrownBy(() -> wallet.earn(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> wallet.deduct(-10)).isInstanceOf(IllegalArgumentException.class);
    }
}
