import { redirect } from "next/navigation";

const CATEGORIES = [
  {
    slug: "fashion",
    subcategories: [
      { slug: "clothing" },
      { slug: "shoes" },
      { slug: "bags" },
      { slug: "eyewear-accessories" },
      { slug: "watches-jewelry" },
    ],
  },
  {
    slug: "beauty",
    subcategories: [
      { slug: "skincare" },
      { slug: "makeup" },
      { slug: "perfume" },
      { slug: "hair-nail" },
    ],
  },
  {
    slug: "living",
    subcategories: [
      { slug: "interior-decor" },
      { slug: "furniture" },
      { slug: "household" },
      { slug: "electronics" },
    ],
  },
  {
    slug: "hobby",
    subcategories: [
      { slug: "camping" },
      { slug: "sports-gear" },
      { slug: "reading" },
      { slug: "stationery" },
    ],
  },
  {
    slug: "digital",
    subcategories: [
      { slug: "audio" },
      { slug: "keyboard-mouse" },
      { slug: "phone-accessories" },
    ],
  },
  {
    slug: "finance",
    subcategories: [
      { slug: "finance-apps" },
      { slug: "card-benefits" },
      { slug: "stocks" },
    ],
  },
];

// /item/cate는 첫 번째 카테고리의 첫 번째 하위 카테고리로 보낸다.
// 이 페이지가 없으면 /item/[id]가 id="cate"로 받아 버린다.
export default function ItemCategoryIndexPage() {
  const [category] = CATEGORIES;
  const [subcategory] = category.subcategories;

  redirect(`/item/cate/${category.slug}/${subcategory.slug}`);
}
