package com.trendizm.backend.lookup.domain.enums;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/** 의견 투표 종류. 요청·응답에는 slug(agree, disagree)로 주고받는다 */
public enum MemeVoteType {

    AGREE("동의"),
    DISAGREE("비동의");

    private final String label;

    MemeVoteType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String slug() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** 대소문자·앞뒤 공백은 무시한다. 없는 값이면 empty */
    public static Optional<MemeVoteType> fromSlug(String slug) {
        if (slug == null) {
            return Optional.empty();
        }
        String normalized = slug.strip();
        return Arrays.stream(values()).filter(type -> type.slug().equalsIgnoreCase(normalized)).findFirst();
    }
}
