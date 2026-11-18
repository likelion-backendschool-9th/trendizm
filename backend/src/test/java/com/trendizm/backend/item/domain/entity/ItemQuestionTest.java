package com.trendizm.backend.item.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.trendizm.backend.item.domain.enums.ItemCategory;
import com.trendizm.backend.item.domain.enums.ItemQuestionStatus;
import com.trendizm.backend.item.domain.enums.ItemSubcategory;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ItemQuestionTest {

    @Test
    @DisplayName("소분류로 대분류를 채우고, 처음에는 공개 상태다")
    void 생성() {
        ItemQuestion question = ItemQuestion.create(1L, ItemSubcategory.EYEWEAR_ACCESSORIES, "제목", "내용");

        assertThat(question.getCategory()).isEqualTo(ItemCategory.FASHION);
        assertThat(question.getStatus()).isEqualTo(ItemQuestionStatus.PUBLIC);
        assertThat(question.isPublic()).isTrue();
    }

    @Test
    @DisplayName("관리자가 숨기면 공개 상태가 아니다")
    void 숨김() {
        ItemQuestion question = ItemQuestion.create(1L, ItemSubcategory.CLOTHING, "제목", "내용");

        question.changeStatus(ItemQuestionStatus.HIDDEN);

        assertThat(question.isPublic()).isFalse();
    }

    @Test
    @DisplayName("소분류 23개가 대분류 6개에 나뉘어 있고, slug는 소문자와 하이픈이다")
    void 카테고리() {
        assertThat(ItemCategory.values()).hasSize(6);
        assertThat(ItemSubcategory.values()).hasSize(23);
        assertThat(Arrays.stream(ItemSubcategory.values()).map(ItemSubcategory::getCategory).distinct())
                .containsExactlyInAnyOrder(ItemCategory.values());
        assertThat(ItemSubcategory.EYEWEAR_ACCESSORIES.getSlug()).isEqualTo("eyewear-accessories");
        assertThat(ItemCategory.FASHION.getSlug()).isEqualTo("fashion");
    }
}
