// 호불호 평점 카테고리. 백엔드 rating.domain.enums의 RatingContentCategory·RatingContentSubcategory·RatingContentStatus와
// slug·이름이 같다. 목록·상세 화면이 모두 이 파일을 쓴다. 카테고리를 바꾸면 백엔드 enum도 같이 바꾼다.

export type RatingSubcategory = { slug: string; name: string };

export const RATING_STATUSES = ["upcoming", "ongoing", "ended"] as const;
export type RatingStatusSlug = (typeof RATING_STATUSES)[number];

export type RatingCategory = {
  slug: string;
  name: string;
  // 대분류 칸에 보이는 아이콘. public 폴더 기준 SVG 경로
  icon: string;
  subcategories: RatingSubcategory[];
  // 진행 상태 화면 이름 (백엔드 RatingContentStatus의 대분류별 이름과 같다)
  statusLabels: Record<RatingStatusSlug, string>;
};

/** 대분류를 고르지 않았을 때(전체) 진행 상태 이름 */
export const DEFAULT_STATUS_LABELS: Record<RatingStatusSlug, string> = {
  upcoming: "예정",
  ongoing: "진행중",
  ended: "종료",
};

export const RATING_CATEGORIES: RatingCategory[] = [
  {
    slug: "media",
    name: "미디어",
    icon: "/images/rating/media.svg",
    subcategories: [
      { slug: "movie", name: "영화" },
      { slug: "drama", name: "드라마" },
      { slug: "variety", name: "예능" },
      { slug: "animation", name: "애니메이션" },
    ],
    statusLabels: { upcoming: "공개 예정", ongoing: "방영중", ended: "완결" },
  },
  {
    slug: "food",
    name: "푸드",
    icon: "/images/rating/food.svg",
    subcategories: [
      { slug: "dish", name: "음식" },
      { slug: "dessert", name: "디저트" },
      { slug: "beverage", name: "음료" },
      { slug: "new-product", name: "편의점·프랜차이즈 신상" },
    ],
    statusLabels: { upcoming: "출시 예정", ongoing: "판매중", ended: "판매종료" },
  },
  {
    slug: "music",
    name: "음악",
    icon: "/images/rating/music.svg",
    subcategories: [
      { slug: "album", name: "앨범" },
      { slug: "song", name: "노래" },
      { slug: "concert-festival", name: "공연·페스티벌" },
    ],
    statusLabels: { upcoming: "발매·공연 예정", ongoing: "발매·공연중", ended: "종료" },
  },
  {
    slug: "books",
    name: "도서·웹툰",
    icon: "/images/rating/books.svg",
    subcategories: [
      { slug: "book", name: "책" },
      { slug: "webtoon", name: "웹툰" },
      { slug: "web-novel", name: "웹소설" },
    ],
    statusLabels: { upcoming: "연재 예정", ongoing: "연재중", ended: "완결" },
  },
  {
    slug: "game",
    name: "게임",
    icon: "/images/rating/game.svg",
    subcategories: [
      { slug: "pc-console", name: "PC·콘솔" },
      { slug: "mobile", name: "모바일" },
    ],
    statusLabels: { upcoming: "출시 예정", ongoing: "서비스중", ended: "서비스 종료" },
  },
  {
    slug: "place",
    name: "장소",
    icon: "/images/rating/place.svg",
    subcategories: [
      { slug: "cafe", name: "카페" },
      { slug: "exhibition-popup", name: "전시·팝업" },
      { slug: "travel", name: "여행지" },
    ],
    statusLabels: { upcoming: "오픈 예정", ongoing: "진행중", ended: "종료" },
  },
];

export function findCategory(slug: string): RatingCategory | undefined {
  return RATING_CATEGORIES.find((category) => category.slug === slug);
}

/** 대분류 안의 소분류. 다른 대분류의 소분류면 undefined */
export function findSubcategory(category: RatingCategory, slug: string): RatingSubcategory | undefined {
  return category.subcategories.find((subcategory) => subcategory.slug === slug);
}

export function isRatingStatus(value: string | undefined): value is RatingStatusSlug {
  return RATING_STATUSES.some((status) => status === value);
}

/** "몇 화까지 봤는지"를 물어볼 소분류 (회차가 있는 것) */
export const PROGRESS_SUBCATEGORIES = new Set(["drama", "variety", "animation", "webtoon", "web-novel"]);
