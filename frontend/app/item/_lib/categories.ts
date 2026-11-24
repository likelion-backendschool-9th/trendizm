// 트렌드 아이템 카테고리. 백엔드 item.domain.enums.ItemCategory·ItemSubcategory와 slug·이름이 같다.
// 목록·상세·질문 작성 화면이 모두 이 파일을 쓴다. 카테고리를 바꾸면 백엔드 enum도 같이 바꾼다.

export type ItemSubcategory = { slug: string; name: string };

export type ItemCategory = {
  slug: string;
  name: string;
  subcategories: ItemSubcategory[];
};

export const ITEM_CATEGORIES: ItemCategory[] = [
  {
    slug: "fashion",
    name: "패션",
    subcategories: [
      { slug: "clothing", name: "의류" },
      { slug: "shoes", name: "신발" },
      { slug: "bags", name: "가방" },
      { slug: "eyewear-accessories", name: "안경·액세서리" },
      { slug: "watches-jewelry", name: "시계·주얼리" },
    ],
  },
  {
    slug: "beauty",
    name: "뷰티",
    subcategories: [
      { slug: "skincare", name: "스킨케어" },
      { slug: "makeup", name: "메이크업" },
      { slug: "perfume", name: "향수" },
      { slug: "hair-nail", name: "헤어·네일" },
    ],
  },
  {
    slug: "living",
    name: "리빙",
    subcategories: [
      { slug: "interior-decor", name: "인테리어 소품" },
      { slug: "furniture", name: "가구" },
      { slug: "household", name: "생활용품" },
      { slug: "electronics", name: "전자기기" },
    ],
  },
  {
    slug: "hobby",
    name: "취미",
    subcategories: [
      { slug: "camping", name: "캠핑" },
      { slug: "sports-gear", name: "운동용품" },
      { slug: "reading", name: "독서" },
      { slug: "stationery", name: "문구" },
    ],
  },
  {
    slug: "digital",
    name: "디지털",
    subcategories: [
      { slug: "audio", name: "이어폰·헤드폰" },
      { slug: "keyboard-mouse", name: "키보드·마우스" },
      { slug: "phone-accessories", name: "폰 액세서리" },
    ],
  },
  {
    slug: "finance",
    name: "재테크",
    subcategories: [
      { slug: "finance-apps", name: "재테크 앱" },
      { slug: "card-benefits", name: "카드 혜택" },
      { slug: "stocks", name: "주식" },
    ],
  },
];

export function findCategory(slug: string): ItemCategory | undefined {
  return ITEM_CATEGORIES.find((category) => category.slug === slug);
}

/** 대분류 안의 소분류. 다른 대분류의 소분류면 undefined */
export function findSubcategory(category: ItemCategory, slug: string): ItemSubcategory | undefined {
  return category.subcategories.find((subcategory) => subcategory.slug === slug);
}
