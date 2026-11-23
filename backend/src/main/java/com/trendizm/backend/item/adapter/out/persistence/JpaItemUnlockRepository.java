package com.trendizm.backend.item.adapter.out.persistence;

import com.trendizm.backend.item.application.port.out.ItemUnlockRepository;
import com.trendizm.backend.item.domain.entity.ItemUnlock;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** ItemUnlockRepository(out 포트)의 메서드를 Spring Data가 구현한다 */
public interface JpaItemUnlockRepository extends JpaRepository<ItemUnlock, Long>, ItemUnlockRepository {

    /**
     * (item_id, user_id) 유니크 제약에 걸리면 넣지 않는다. 충돌 대상을 적지 않은 형태는 H2도 지원한다.
     * PostgreSQL에서는 같은 행을 넣는 다른 트랜잭션이 끝날 때까지 기다렸다가, 그쪽이 롤백되면 이쪽이 넣는다.
     * 네이티브 INSERT라 JPA Auditing이 동작하지 않아서 created_at을 직접 채운다.
     */
    @Override
    @Modifying
    @Query(value = """
            INSERT INTO item_unlocks (item_id, user_id, price, created_at)
            VALUES (:itemId, :userId, :price, CURRENT_TIMESTAMP)
            ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("itemId") Long itemId, @Param("userId") Long userId, @Param("price") int price);

    @Override
    @Query("select u.itemId from ItemUnlock u where u.userId = :userId and u.itemId in :itemIds")
    List<Long> findUnlockedItemIds(@Param("userId") Long userId, @Param("itemIds") Collection<Long> itemIds);
}
