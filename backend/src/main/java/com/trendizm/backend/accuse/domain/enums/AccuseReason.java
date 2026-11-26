package com.trendizm.backend.accuse.domain.enums;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/** 신고 사유. 화면의 신고 모달에서 고른다. 요청·응답에는 slug(sexual, hate, ad, etc)로 주고받는다 */
public enum AccuseReason {

    SEXUAL("선정적"),
    HATE("혐오·비하"),
    AD("광고"),
    /** 기타. 신고 내용(description)이 필수다 */
    ETC("기타");

    private final String label;

    AccuseReason(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String slug() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** 신고 내용을 반드시 적어야 하는 사유인지 */
    public boolean requiresDescription() {
        return this == ETC;
    }

    /** 대소문자·앞뒤 공백은 무시한다. 없는 값이면 empty */
    public static Optional<AccuseReason> fromSlug(String slug) {
        if (slug == null) {
            return Optional.empty();
        }
        String normalized = slug.strip();
        return Arrays.stream(values()).filter(reason -> reason.slug().equalsIgnoreCase(normalized)).findFirst();
    }
}
