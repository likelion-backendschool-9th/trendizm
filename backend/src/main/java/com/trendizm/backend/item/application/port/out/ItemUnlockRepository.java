package com.trendizm.backend.item.application.port.out;

import com.trendizm.backend.item.domain.entity.ItemUnlock;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** 추천 아이템 열람 기록(item_unlocks) 저장소. 구현체는 adapter/out/persistence/JpaItemUnlockRepository */
public interface ItemUnlockRepository {

    ItemUnlock save(ItemUnlock unlock);

    /**
     * 열람 기록을 추가한다. (item_id, user_id)가 이미 있으면 아무것도 하지 않는다.
     * 같은 회원이 같은 아이템을 동시에 열어도 한 줄만 생긴다.
     * @return 추가한 행 수 (0 또는 1)
     */
    int insertIfAbsent(Long itemId, Long userId, int price);

    Optional<ItemUnlock> findByItemIdAndUserId(Long itemId, Long userId);

    boolean existsByItemIdAndUserId(Long itemId, Long userId);

    /** 이 회원이 열람한 아이템 id (주어진 아이템 중에서) */
    List<Long> findUnlockedItemIds(Long userId, Collection<Long> itemIds);
}
