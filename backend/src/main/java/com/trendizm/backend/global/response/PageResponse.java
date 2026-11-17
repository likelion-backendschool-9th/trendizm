package com.trendizm.backend.global.response;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

/**
 * 목록 API의 공통 응답. 스프링 Page를 그대로 내보내지 않고 필요한 값만 담는다.
 * 예: { "content": [...], "page": 0, "size": 20, "totalElements": 53, "totalPages": 3, "hasNext": true }
 * page는 0부터 시작한다.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {

    /** 이미 DTO로 바뀐 Page를 감쌀 때 */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }

    /** 엔티티 Page를 DTO로 바꾸면서 감쌀 때. 예: PageResponse.from(memePage, MemeSummary::from) */
    public static <E, T> PageResponse<T> from(Page<E> page, Function<? super E, ? extends T> mapper) {
        return from(page.map(mapper::apply));
    }
}
