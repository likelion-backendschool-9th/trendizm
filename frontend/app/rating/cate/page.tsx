import { redirect } from "next/navigation";
import { RATING_CATEGORIES } from "@/app/rating/_lib/categories";

// /rating/cate는 첫 번째 카테고리의 첫 번째 하위 카테고리로 보낸다.
// 하위 카테고리가 없는 카테고리면 그 카테고리로 보낸다.
export default function RatingCategoryIndexPage() {
  const [category] = RATING_CATEGORIES;
  const subcategory = category.subcategories.at(0);

  redirect(
    subcategory
      ? `/rating/cate/${category.slug}/${subcategory.slug}`
      : `/rating/cate/${category.slug}`,
  );
}
