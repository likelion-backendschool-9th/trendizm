package com.trendizm.backend.rating.domain.enums;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * 콘텐츠 추가 정보(rating_contents.extra, jsonb)에 넣을 수 있는 키. 소분류마다 쓸 수 있는 키가 정해져 있다.
 * key는 JSON에 저장하고 화면이 읽는 이름이다(예: { "season": 2, "episodeLength": "60분" }).
 * 화면에 새 항목이 필요하면 여기에 한 줄 추가한다.
 */
public enum RatingContentExtraKey {

    /** 예: 2 → 화면 "시즌 2" */
    SEASON("season", "시즌",
            RatingContentSubcategory.DRAMA, RatingContentSubcategory.VARIETY, RatingContentSubcategory.ANIMATION),
    /** 예: "60분" → 화면 "에피소드당 60분" */
    EPISODE_LENGTH("episodeLength", "에피소드당 길이",
            RatingContentSubcategory.DRAMA, RatingContentSubcategory.VARIETY, RatingContentSubcategory.ANIMATION);

    private final String key;
    private final String label;
    private final Set<RatingContentSubcategory> subcategories;

    RatingContentExtraKey(String key, String label, RatingContentSubcategory... subcategories) {
        this.key = key;
        this.label = label;
        this.subcategories = Set.of(subcategories);
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    /** 이 소분류에서 쓸 수 있는 키인지 */
    public boolean isAllowedFor(RatingContentSubcategory subcategory) {
        return subcategories.contains(subcategory);
    }

    /** JSON 키 이름으로 찾는다. 예: "episodeLength" → EPISODE_LENGTH. 없으면 빈 Optional */
    public static Optional<RatingContentExtraKey> fromKey(String key) {
        return Arrays.stream(values()).filter(value -> value.key.equals(key)).findFirst();
    }

    /** 이 소분류의 추가 정보에 넣을 수 있는 키인지 (없는 키이거나 다른 소분류용 키면 false) */
    public static boolean isAllowed(RatingContentSubcategory subcategory, String key) {
        return fromKey(key).map(extraKey -> extraKey.isAllowedFor(subcategory)).orElse(false);
    }
}
