package com.trendizm.backend.accuse.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.trendizm.backend.accuse.domain.enums.AccuseReason;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AccuseTest {

    @Test
    @DisplayName("새 신고는 처리 대기 중이고, 처리하면 처리 시각이 들어간다")
    void 처리() {
        Accuse accuse = Accuse.create("memes", 1L, 2L, AccuseReason.HATE, null);
        assertThat(accuse.isOpen()).isTrue();

        LocalDateTime now = LocalDateTime.of(2026, 11, 26, 12, 0);
        accuse.close(now);

        assertThat(accuse.isOpen()).isFalse();
        assertThat(accuse.getClosedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("이미 처리된 신고를 다시 처리해도 처음 처리한 시각을 그대로 둔다")
    void 다시_처리() {
        Accuse accuse = Accuse.create("memes", 1L, 2L, AccuseReason.HATE, null);
        LocalDateTime first = LocalDateTime.of(2026, 11, 26, 12, 0);
        accuse.close(first);

        accuse.close(first.plusHours(1));

        assertThat(accuse.getClosedAt()).isEqualTo(first);
    }

    @Test
    @DisplayName("신고 내용은 앞뒤 공백을 지우고, 비어 있으면 null로 저장한다")
    void 내용_정리() {
        assertThat(Accuse.create("memes", 1L, 2L, AccuseReason.ETC, "  광고 링크  ").getDescription())
                .isEqualTo("광고 링크");
        assertThat(Accuse.create("memes", 1L, 2L, AccuseReason.AD, "   ").getDescription()).isNull();
        assertThat(Accuse.create("memes", 1L, 2L, AccuseReason.AD, null).getDescription()).isNull();
    }

    @Test
    @DisplayName("대상 테이블 이름이 비었거나 30자를 넘으면 IllegalArgumentException (호출한 도메인의 잘못)")
    void 잘못된_대상() {
        for (String target : new String[] {null, "", "  ", "a".repeat(31)}) {
            assertThatThrownBy(() -> Accuse.create(target, 1L, 2L, AccuseReason.HATE, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        assertThat(Accuse.create("a".repeat(30), 1L, 2L, AccuseReason.HATE, null).getTarget()).hasSize(30);
    }
}
