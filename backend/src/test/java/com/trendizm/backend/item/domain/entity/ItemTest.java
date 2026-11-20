package com.trendizm.backend.item.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ItemTest {

    @Test
    @DisplayName("열람 가격은 기본 100P이고, 태그가 없으면 빈 목록이다")
    void 기본값() {
        Item item = Item.create(1L, 2L, "양말", null, null, null, "설명", null, null);

        assertThat(item.getUnlockPrice()).isEqualTo(100);
        assertThat(item.getTags()).isEmpty();
    }

    @Test
    @DisplayName("넘긴 태그 목록을 나중에 바꿔도 아이템 태그는 바뀌지 않는다")
    void 태그_복사() {
        List<String> tags = new ArrayList<>(List.of("화이트"));
        Item item = Item.create(1L, 2L, "양말", null, null, tags, "설명", null, null);

        tags.add("폭신한 재질");

        assertThat(item.getTags()).containsExactly("화이트");
    }

    @Test
    @DisplayName("등록자인지 확인한다. 로그인하지 않았으면(null) 등록자가 아니다")
    void 등록자() {
        Item item = Item.create(1L, 2L, "양말", null, null, null, "설명", null, null);

        assertThat(item.isOwnedBy(2L)).isTrue();
        assertThat(item.isOwnedBy(3L)).isFalse();
        assertThat(item.isOwnedBy(null)).isFalse();
    }
}
