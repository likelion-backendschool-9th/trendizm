package com.trendizm.backend.item.application.port;

import com.trendizm.backend.item.application.dto.ItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** 컬럼이 정해지면 createItem·updateItem에 파라미터를 추가한다. */
public interface ItemService {

    Page<ItemResponse> getItems(Pageable pageable);

    /** 없으면 예외를 던진다 */
    ItemResponse getItem(Long itemId);

    /** @return 생성한 아이템 id */
    Long createItem();

    void updateItem(Long itemId);

    void deleteItem(Long itemId);
}
