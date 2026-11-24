import { redirect } from "next/navigation";

// /item/cate는 전체 목록으로 보낸다.
// 이 페이지가 없으면 /item/[id]가 id="cate"로 받아 버린다.
export default function ItemCategoryIndexPage() {
  redirect("/item");
}
