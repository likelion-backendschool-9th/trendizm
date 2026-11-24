package com.trendizm.backend.item.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ItemUnlockTest {

    @Test
    @DisplayName("열람 당시 가격을 기록한다")
    void 생성() {
        ItemUnlock unlock = ItemUnlock.create(1L, 2L, 100);

        assertThat(unlock.getItemId()).isEqualTo(1L);
        assertThat(unlock.getUserId()).isEqualTo(2L);
        assertThat(unlock.getPrice()).isEqualTo(100);
    }

    @Test
    @DisplayName("열람 가격이 0 이하면 IllegalArgumentException")
    void 잘못된_가격() {
        assertThatThrownBy(() -> ItemUnlock.create(1L, 2L, 0)).isInstanceOf(IllegalArgumentException.class);
    }
}
