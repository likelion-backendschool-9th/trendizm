package com.trendizm.backend.item.domain.enums;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * 트렌드 아이템 소분류 (ERD item_questions.subcategory, varchar(30)).
 * 소분류마다 대분류가 정해져 있어서, 질문을 만들 때 소분류만 받고 대분류는 여기서 채운다.
 * slug는 화면 주소에 쓰는 이름이다. 예: /item/cate/fashion/eyewear-accessories
 */
public enum ItemSubcategory {

    CLOTHING(ItemCategory.FASHION, "의류"),
    SHOES(ItemCategory.FASHION, "신발"),
    BAGS(ItemCategory.FASHION, "가방"),
    EYEWEAR_ACCESSORIES(ItemCategory.FASHION, "안경·액세서리"),
    WATCHES_JEWELRY(ItemCategory.FASHION, "시계·주얼리"),

    SKINCARE(ItemCategory.BEAUTY, "스킨케어"),
    MAKEUP(ItemCategory.BEAUTY, "메이크업"),
    PERFUME(ItemCategory.BEAUTY, "향수"),
    HAIR_NAIL(ItemCategory.BEAUTY, "헤어·네일"),

    INTERIOR_DECOR(ItemCategory.LIVING, "인테리어 소품"),
    FURNITURE(ItemCategory.LIVING, "가구"),
    HOUSEHOLD(ItemCategory.LIVING, "생활용품"),
    ELECTRONICS(ItemCategory.LIVING, "전자기기"),

    CAMPING(ItemCategory.HOBBY, "캠핑"),
    SPORTS_GEAR(ItemCategory.HOBBY, "운동용품"),
    READING(ItemCategory.HOBBY, "독서"),
    STATIONERY(ItemCategory.HOBBY, "문구"),

    AUDIO(ItemCategory.DIGITAL, "이어폰·헤드폰"),
    KEYBOARD_MOUSE(ItemCategory.DIGITAL, "키보드·마우스"),
    PHONE_ACCESSORIES(ItemCategory.DIGITAL, "폰 액세서리"),

    FINANCE_APPS(ItemCategory.FINANCE, "재테크 앱"),
    CARD_BENEFITS(ItemCategory.FINANCE, "카드 혜택"),
    STOCKS(ItemCategory.FINANCE, "주식");

    private final ItemCategory category;
    private final String label;

    ItemSubcategory(ItemCategory category, String label) {
        this.category = category;
        this.label = label;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public String getLabel() {
        return label;
    }

    /** 예: EYEWEAR_ACCESSORIES → eyewear-accessories */
    public String getSlug() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    /** slug로 찾는다. 예: "eyewear-accessories" → EYEWEAR_ACCESSORIES. 없으면 빈 Optional */
    public static Optional<ItemSubcategory> fromSlug(String slug) {
        return Arrays.stream(values()).filter(value -> value.getSlug().equals(slug)).findFirst();
    }
}
