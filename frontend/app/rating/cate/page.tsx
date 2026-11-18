import { redirect } from "next/navigation";

const CATEGORIES: { slug: string; subcategories: { slug: string }[] }[] = [
  {
    slug: "media",
    subcategories: [
      { slug: "airing" },
      { slug: "ended" },
    ],
  },
  {
    slug: "food",
    subcategories: [
      { slug: "new" },
      { slug: "discontinued" },
    ],
  },
  {
    slug: "music",
    subcategories: [
      { slug: "new-release" },
      { slug: "upcoming-concert" },
    ],
  },
  {
    slug: "books",
    subcategories: [
      { slug: "ongoing" },
      { slug: "completed" },
    ],
  },
  {
    slug: "game",
    subcategories: [
      { slug: "upcoming" },
      { slug: "live" },
    ],
  },
  {
    slug: "place",
    subcategories: [
      { slug: "ongoing" },
      { slug: "ended" },
    ],
  },
];

// /rating/cate는 첫 번째 카테고리의 첫 번째 하위 카테고리로 보낸다.
// 하위 카테고리가 없는 카테고리면 그 카테고리로 보낸다.
export default function RatingCategoryIndexPage() {
  const [category] = CATEGORIES;
  const subcategory = category.subcategories.at(0);

  redirect(
    subcategory
      ? `/rating/cate/${category.slug}/${subcategory.slug}`
      : `/rating/cate/${category.slug}`,
  );
}
