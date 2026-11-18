package com.trendizm.backend.point.application.port.out;

import com.trendizm.backend.point.domain.PointHistory;
import com.trendizm.backend.point.domain.PointType;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** 구현체는 adapter/out/persistence/JpaPointHistoryRepository (Spring Data가 자동 구현). */
public interface PointHistoryRepository {

    PointHistory save(PointHistory history);

    Page<PointHistory> findByUserIdAndTypeIn(Long userId, Collection<PointType> types, Pageable pageable);

    boolean existsByItemUnlockIdAndType(Long itemUnlockId, PointType type);

    boolean existsByRatingIdAndType(Long ratingId, PointType type);
}
