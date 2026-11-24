import { redirect } from "next/navigation";

// /rating/cate는 전체 목록으로 보낸다.
export default function RatingCategoryIndexPage() {
  redirect("/rating");
}
