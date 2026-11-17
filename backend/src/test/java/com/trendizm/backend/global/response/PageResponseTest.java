package com.trendizm.backend.global.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class PageResponseTest {

    @Test
    @DisplayName("Page의 내용과 페이지 정보를 그대로 옮긴다")
    void 페이지_정보_변환() {
        PageImpl<String> page = new PageImpl<>(List.of("a", "b"), PageRequest.of(0, 2), 5);

        PageResponse<String> response = PageResponse.from(page);

        assertThat(response.content()).containsExactly("a", "b");
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.totalElements()).isEqualTo(5);
        assertThat(response.totalPages()).isEqualTo(3);
        assertThat(response.hasNext()).isTrue();
    }

    @Test
    @DisplayName("마지막 페이지면 hasNext가 false다")
    void 마지막_페이지() {
        PageImpl<String> page = new PageImpl<>(List.of("e"), PageRequest.of(2, 2), 5);

        PageResponse<String> response = PageResponse.from(page);

        assertThat(response.page()).isEqualTo(2);
        assertThat(response.hasNext()).isFalse();
    }

    @Test
    @DisplayName("변환 함수를 넘기면 내용을 DTO로 바꿔서 담는다")
    void 내용_변환() {
        PageImpl<Integer> page = new PageImpl<>(List.of(1, 2), PageRequest.of(0, 2), 2);

        PageResponse<String> response = PageResponse.from(page, number -> "#" + number);

        assertThat(response.content()).containsExactly("#1", "#2");
        assertThat(response.hasNext()).isFalse();
    }
}
