import { Suspense } from "react";
import ItemList, {
  type ItemCategory,
  type ItemPost,
} from "@/app/item/_components/ItemList";

const CATEGORIES: ItemCategory[] = [
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

const TOTAL_COUNT = 42;

const POSTS: ItemPost[] = [
  {
    id: 3,
    subcategory: "안경·액세서리",
    status: "solved",
    title: "질문 제목",
    body: "질문 내용 미리보기입니다. 두 줄이 넘어가면 말줄임표로 잘려서 카드 높이가 항상 일정하게 유지됩니다.",
    author: "홍길동",
    createdAt: "2026.11.14",
    commentCount: 10,
    viewCount: 248,
    recommendation: { locked: true, count: 3 },
  },
  {
    id: 1,
    subcategory: "의류",
    status: "waiting",
    title: "양말 추천해주세요ㅠㅠ",
    body: "양말을 사고 싶은데, 제가 원하는 스타일이나 저한테 딱 맞는 양말이 없네요... 세련된 스타일의 양말 중 화이트 계열 색상의 따뜻한 느낌에 재질은 폭신폭신한 양말 어디 없나요~?",
    author: "민지",
    createdAt: "2026.11.13",
    commentCount: 2,
    viewCount: 132,
    recommendation: {
      locked: false,
      items: [
        {
          image: "/images/item/socks.jpg",
          name: "아이템 이름",
          price: "10,000원",
        },
      ],
    },
  },
  {
    id: 2,
    subcategory: "신발",
    status: "waiting",
    title: "편하면서 밤티 안나는 신발 없나요??",
    body: "신을 때 편하면서 편하면서 밤티 안나는 신발 없나요??",
    author: "준호",
    createdAt: "2026.11.11",
    commentCount: 4,
    viewCount: 189,
    recommendation: {
      locked: false,
      items: [
        {
          image: "/images/item/shoes.webp",
          name: "라이트웨잇 스니커즈",
          price: "30,000원",
        },
      ],
    },
  },
];

export default function ItemCategoryPage({
  params,
}: PageProps<"/item/cate/[cate]">) {
  return (
    <Suspense fallback={<p>불러오는 중...</p>}>
      <ItemCategoryList params={params} />
    </Suspense>
  );
}

async function ItemCategoryList({
  params,
}: Pick<PageProps<"/item/cate/[cate]">, "params">) {
  const { cate } = await params;

  return (
    <ItemList
      categories={CATEGORIES}
      cate={cate}
      totalCount={TOTAL_COUNT}
      posts={POSTS}
    />
  );
}
