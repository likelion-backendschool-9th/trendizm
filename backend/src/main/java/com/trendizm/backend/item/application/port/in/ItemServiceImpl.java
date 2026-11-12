package com.trendizm.backend.item.application.port.in;

import com.trendizm.backend.item.application.dto.ItemResponse;
import com.trendizm.backend.item.application.port.ItemService;
import com.trendizm.backend.item.application.port.out.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    @Override
    public Page<ItemResponse> getItems(Pageable pageable) {
        // TODO(item): 아이템 목록 조회
        //  1. itemRepository.findAll(pageable)
        //  2. .map(ItemResponse::from) 으로 변환해 반환
        //  (숏폼·롱폼 탭, 해시태그 조회는 컬럼이 정해지면 ItemRepository에 findByXxx를 추가)
        throw new UnsupportedOperationException("TODO(item): 아이템 목록 조회");
    }

    @Override
    public ItemResponse getItem(Long itemId) {
        // TODO(item): 아이템 단건 조회
        //  1. itemRepository.findById(itemId), 없으면 예외를 던진다
        //  2. ItemResponse.from(item) 반환
        throw new UnsupportedOperationException("TODO(item): 아이템 단건 조회");
    }

    @Override
    @Transactional
    public Long createItem() {
        // TODO(item): 아이템 등록
        //  1. Item.create(...) (영상 링크 → 임베드 URL 변환은 여기서)
        //  2. itemRepository.save(item).getId() 반환
        throw new UnsupportedOperationException("TODO(item): 아이템 등록");
    }

    @Override
    @Transactional
    public void updateItem(Long itemId) {
        // TODO(item): 아이템 수정
        //  1. itemRepository.findById(itemId), 없으면 NOT_FOUND
        //  2. Item에 update(...) 메서드를 만들어 호출 (트랜잭션 안이라 변경 감지로 저장된다)
        throw new UnsupportedOperationException("TODO(item): 아이템 수정");
    }

    @Override
    @Transactional
    public void deleteItem(Long itemId) {
        // TODO(item): 아이템 삭제
        //  1. itemRepository.findById(itemId), 없으면 NOT_FOUND
        //  2. itemRepository.delete(item)
        throw new UnsupportedOperationException("TODO(item): 아이템 삭제");
    }
}
