package com.trendizm.backend.item.adapter.out.persistence;

import com.trendizm.backend.item.application.port.out.ItemRepository;
import com.trendizm.backend.item.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

/** ItemRepository(out 포트)의 메서드를 Spring Data가 구현한다. 서비스는 ItemRepository만 알고 이 인터페이스는 모른다. */
public interface JpaItemRepository extends JpaRepository<Item, Long>, ItemRepository {
}
