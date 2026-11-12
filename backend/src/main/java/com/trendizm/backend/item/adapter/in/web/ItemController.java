package com.trendizm.backend.item.adapter.in.web;

import com.trendizm.backend.item.adapter.in.web.dto.ItemRequest;
import com.trendizm.backend.item.application.dto.ItemResponse;
import com.trendizm.backend.item.application.port.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 로그인 없이 모든 요청 허용 (auth/adapter/out/security/SecurityConfig) */
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public Page<ItemResponse> getItems(Pageable pageable) {
        return itemService.getItems(pageable);
    }

    @GetMapping("/{itemId}")
    public ItemResponse getItem(@PathVariable Long itemId) {
        return itemService.getItem(itemId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long createItem(@Valid @RequestBody ItemRequest request) {
        return itemService.createItem();
    }

    @PutMapping("/{itemId}")
    public void updateItem(@PathVariable Long itemId, @Valid @RequestBody ItemRequest request) {
        itemService.updateItem(itemId);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
    }
}
