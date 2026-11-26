package com.trendizm.backend.accuse.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.trendizm.backend.accuse.domain.enums.AccuseReason;
import com.trendizm.backend.accuse.domain.enums.AccuseTarget;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AccuseTest {

    @Test
    @DisplayName("새 신고는 처리 대기 중이고, 처리하면 처리 시각이 들어간다")
    void 처리() {
        Accuse accuse = Accuse.create(AccuseTarget.MEME, 1L, 2L, AccuseReason.HATE, null);
        assertThat(accuse.isOpen()).isTrue();

        LocalDateTime now = LocalDateTime.of(2026, 11, 26, 12, 0);
        accuse.close(now);

        assertThat(accuse.isOpen()).isFalse();
        assertThat(accuse.getClosedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("이미 처리된 신고를 다시 처리해도 처음 처리한 시각을 그대로 둔다")
    void 다시_처리() {
        Accuse accuse = Accuse.create(AccuseTarget.MEME, 1L, 2L, AccuseReason.HATE, null);
        LocalDateTime first = LocalDateTime.of(2026, 11, 26, 12, 0);
        accuse.close(first);

        accuse.close(first.plusHours(1));

        assertThat(accuse.getClosedAt()).isEqualTo(first);
    }

    @Test
    @DisplayName("신고 내용은 앞뒤 공백을 지우고, 비어 있으면 null로 저장한다")
    void 내용_정리() {
        assertThat(Accuse.create(AccuseTarget.MEME, 1L, 2L, AccuseReason.ETC, "  광고 링크  ").getDescription())
                .isEqualTo("광고 링크");
        assertThat(Accuse.create(AccuseTarget.MEME, 1L, 2L, AccuseReason.AD, "   ").getDescription()).isNull();
        assertThat(Accuse.create(AccuseTarget.MEME, 1L, 2L, AccuseReason.AD, null).getDescription()).isNull();
    }
}
