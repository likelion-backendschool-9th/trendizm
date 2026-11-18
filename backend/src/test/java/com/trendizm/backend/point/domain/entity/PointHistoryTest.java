package com.trendizm.backend.point.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.trendizm.backend.point.domain.enums.PointType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PointHistoryTest {

    @Test
    @DisplayName("차감 내역은 금액이 음수이고, 열람 id만 채운다")
    void 차감_내역() {
        PointWallet wallet = PointWallet.create(1L);
        wallet.earn(500);
        wallet.deduct(100);

        PointHistory history = PointHistory.create(wallet, PointType.UNLOCK, 100, 7L);

        assertThat(history.getUserId()).isEqualTo(1L);
        assertThat(history.getAmount()).isEqualTo(-100);
        assertThat(history.getBalanceAfter()).isEqualTo(400);
        assertThat(history.getItemUnlockId()).isEqualTo(7L);
        assertThat(history.getChargeId()).isNull();
        assertThat(history.getRatingId()).isNull();
    }

    @Test
    @DisplayName("보상 내역은 금액이 양수이고, 후기 id만 채운다")
    void 보상_내역() {
        PointWallet wallet = PointWallet.create(1L);
        wallet.earn(30);

        PointHistory history = PointHistory.create(wallet, PointType.REWARD, 30, 9L);

        assertThat(history.getAmount()).isEqualTo(30);
        assertThat(history.getRatingId()).isEqualTo(9L);
        assertThat(history.getItemUnlockId()).isNull();
    }

    @Test
    @DisplayName("참조 id 없이는 내역을 만들 수 없다")
    void 참조_id_필수() {
        PointWallet wallet = PointWallet.create(1L);

        assertThatThrownBy(() -> PointHistory.create(wallet, PointType.EARN, 10, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
