package com.trendizm.backend.item.application.port.out;

import com.trendizm.backend.item.domain.Item;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 구현체는 adapter/out/persistence/JpaItemRepository (Spring Data가 자동 구현).
 * 메서드 이름을 Spring Data 규칙(findByXxx 등)대로 추가하면 쿼리를 따로 쓰지 않아도 된다.
 */
public interface ItemRepository {

    Item save(Item item);

    Optional<Item> findById(Long id);

    Page<Item> findAll(Pageable pageable);

    void delete(Item item);
}
