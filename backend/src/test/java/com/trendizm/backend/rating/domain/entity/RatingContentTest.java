package com.trendizm.backend.rating.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.trendizm.backend.rating.domain.enums.RatingContentCategory;
import com.trendizm.backend.rating.domain.enums.RatingContentStatus;
import com.trendizm.backend.rating.domain.enums.RatingContentSubcategory;
import com.trendizm.backend.rating.domain.enums.RatingContentVisibility;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RatingContentTest {

    private static RatingContent drama() {
        return RatingContent.create(RatingContentSubcategory.DRAMA, RatingContentStatus.ONGOING, "드라마 제목",
                null, null, 15, null, null);
    }

    @Test
    @DisplayName("소분류로 대분류를 채우고, 처음에는 공개 상태다")
    void 생성() {
        RatingContent content = drama();

        assertThat(content.getCategory()).isEqualTo(RatingContentCategory.MEDIA);
        assertThat(content.getVisibility()).isEqualTo(RatingContentVisibility.PUBLIC);
        assertThat(content.isPublic()).isTrue();
        assertThat(content.getExtra()).isEmpty();
    }

    @Test
    @DisplayName("관리자가 숨기면 공개 상태가 아니고, 진행 상태와는 별개다")
    void 숨김() {
        RatingContent content = drama();

        content.changeVisibility(RatingContentVisibility.HIDDEN);

        assertThat(content.isPublic()).isFalse();
        assertThat(content.getStatus()).isEqualTo(RatingContentStatus.ONGOING);
    }

    @Test
    @DisplayName("진행 상태 화면 이름은 대분류마다 다르다")
    void 진행_상태_이름() {
        RatingContent content = drama();
        assertThat(content.getStatusLabel()).isEqualTo("방영중");

        content.changeStatus(RatingContentStatus.ENDED);
        assertThat(content.getStatusLabel()).isEqualTo("완결");

        assertThat(RatingContentStatus.ONGOING.labelFor(RatingContentCategory.GAME)).isEqualTo("서비스중");
        for (RatingContentStatus status : RatingContentStatus.values()) {
            for (RatingContentCategory category : RatingContentCategory.values()) {
                assertThat(status.labelFor(category)).as("%s %s", status, category).isNotBlank();
            }
        }
    }

    @Test
    @DisplayName("넘긴 추가 정보 맵을 나중에 바꿔도 콘텐츠의 추가 정보는 바뀌지 않는다")
    void 추가_정보_복사() {
        Map<String, Object> extra = new HashMap<>(Map.of("season", 2));
        RatingContent content = RatingContent.create(RatingContentSubcategory.DRAMA, RatingContentStatus.ONGOING,
                "드라마", null, null, null, null, extra);

        extra.put("episodeLength", "60분");

        assertThat(content.getExtra()).containsOnlyKeys("season");
    }

    @Test
    @DisplayName("추가 정보에 빈 값(null)이 있어도 만들 수 있다")
    void 추가_정보_null() {
        Map<String, Object> extra = new HashMap<>();
        extra.put("season", null);

        RatingContent content = RatingContent.create(RatingContentSubcategory.DRAMA, RatingContentStatus.ONGOING,
                "드라마", null, null, null, null, extra);

        assertThat(content.getExtra()).containsEntry("season", null);
    }

    @Test
    @DisplayName("추가 정보 키는 소분류마다 정해진 것만 쓸 수 있다")
    void 추가_정보_키() {
        RatingContent drama = RatingContent.create(RatingContentSubcategory.DRAMA, RatingContentStatus.ONGOING,
                "드라마", null, null, null, null, Map.of("season", 2, "episodeLength", "60분"));
        assertThat(drama.getExtra()).containsOnlyKeys("season", "episodeLength");

        assertThatThrownBy(() -> RatingContent.create(RatingContentSubcategory.CAFE, RatingContentStatus.ONGOING,
                "카페", null, null, null, null, Map.of("season", 2)))
                .as("카페에는 시즌을 넣을 수 없다").isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RatingContent.create(RatingContentSubcategory.DRAMA, RatingContentStatus.ONGOING,
                "드라마", null, null, null, null, Map.of("seson", 2)))
                .as("없는 키").isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("소분류 19개가 대분류 6개에 나뉘어 있고, slug는 소문자와 하이픈이다")
    void 카테고리() {
        assertThat(RatingContentCategory.values()).hasSize(6);
        assertThat(RatingContentSubcategory.values()).hasSize(19);
        assertThat(Arrays.stream(RatingContentSubcategory.values()).map(RatingContentSubcategory::getCategory).distinct())
                .containsExactlyInAnyOrder(RatingContentCategory.values());
        assertThat(RatingContentSubcategory.CONCERT_FESTIVAL.getSlug()).isEqualTo("concert-festival");
        assertThat(RatingContentCategory.BOOKS.getSlug()).isEqualTo("books");
    }
}
