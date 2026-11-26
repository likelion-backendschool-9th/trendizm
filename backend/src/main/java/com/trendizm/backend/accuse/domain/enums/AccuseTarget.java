package com.trendizm.backend.accuse.domain.enums;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * 신고 대상 종류. accuses.target에 이름 그대로 저장한다(테이블 이름이 아니라 이 값).
 * 다른 도메인은 이 enum을 import할 수 없어서 slug(meme 등)로 넘긴다. 대상이 늘면 값을 추가한다.
 */
public enum AccuseTarget {

    /** 밈 문서. target_id는 memes.id */
    MEME("밈");

    private final String label;

    AccuseTarget(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String slug() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** 대소문자·앞뒤 공백은 무시한다. 없는 값이면 empty */
    public static Optional<AccuseTarget> fromSlug(String slug) {
        if (slug == null) {
            return Optional.empty();
        }
        String normalized = slug.strip();
        return Arrays.stream(values()).filter(target -> target.slug().equalsIgnoreCase(normalized)).findFirst();
    }
}
