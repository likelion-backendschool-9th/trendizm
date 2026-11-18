package com.trendizm.backend.item.adapter.in.web.dto;

import com.trendizm.backend.item.application.dto.CreateItemCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 추천 아이템 등록. title·description만 필수다.
 * imageUrl·linkUrl은 화면에서 링크·이미지로 쓰므로 http(s) 주소만 받는다 (javascript: 같은 주소로 스크립트가 실행되지 않게).
 */
public record CreateItemRequest(
        @NotBlank(message = "아이템 이름을 입력해 주세요.")
        @Size(max = 100, message = "아이템 이름은 100자 이하여야 합니다.")
        String title,

        @Size(max = 100, message = "판매처는 100자 이하여야 합니다.")
        String seller,

        @PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
        Integer price,

        @Size(max = 10, message = "태그는 10개 이하여야 합니다.")
        List<@NotBlank(message = "빈 태그는 넣을 수 없습니다.") @Size(max = 20, message = "태그는 20자 이하여야 합니다.") String> tags,

        @NotBlank(message = "설명을 입력해 주세요.")
        @Size(max = 2000, message = "설명은 2000자 이하여야 합니다.")
        String description,

        @Size(max = 500, message = "이미지 주소는 500자 이하여야 합니다.")
        @Pattern(regexp = "^https?://\\S+$", message = "이미지 주소는 http(s)로 시작해야 합니다.")
        String imageUrl,

        @Size(max = 500, message = "링크는 500자 이하여야 합니다.")
        @Pattern(regexp = "^https?://\\S+$", message = "링크는 http(s)로 시작해야 합니다.")
        String linkUrl
) {

    public CreateItemCommand toCommand(Long questionId, Long userId) {
        return new CreateItemCommand(questionId, userId, title.strip(), blankToNull(seller), price,
                tags == null ? List.of() : tags.stream().map(String::strip).toList(),
                description.strip(), blankToNull(imageUrl), blankToNull(linkUrl));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
