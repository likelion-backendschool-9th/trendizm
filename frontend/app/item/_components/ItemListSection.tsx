import { notFound } from "next/navigation";
import ItemList from "@/app/item/_components/ItemList";
import type { ItemQuestionSummary } from "@/app/item/_lib/types";
import { serverFetch } from "@/lib/api/server";
import { ApiError, toApiPage, type PageResponse } from "@/lib/api/types";

const PAGE_SIZE = 10;

type ItemListSectionProps = {
  cate?: string;
  subcate?: string;
  searchParams: Promise<Record<string, string | string[] | undefined>>;
};

// 질문 글 목록을 불러와 보여 준다. 세 목록 페이지가 <Suspense> 안에서 같이 쓴다.
// 없는 카테고리 slug면 백엔드가 400 ITEM_INVALID_CATEGORY를 주므로 404 화면을 보여 준다.
export default async function ItemListSection({
  cate,
  subcate,
  searchParams,
}: ItemListSectionProps) {
  const { page } = await searchParams;
  const query = new URLSearchParams({
    page: String(toApiPage(Array.isArray(page) ? page[0] : page)),
    size: String(PAGE_SIZE),
  });
  if (cate) {
    query.set("category", cate);
  }
  if (subcate) {
    query.set("subcategory", subcate);
  }

  let questions: PageResponse<ItemQuestionSummary>;
  try {
    questions = await serverFetch<PageResponse<ItemQuestionSummary>>(`/api/items?${query}`);
  } catch (error) {
    if (error instanceof ApiError && error.code === "ITEM_INVALID_CATEGORY") {
      notFound();
    }
    throw error;
  }

  return <ItemList cate={cate} subcate={subcate} questions={questions} />;
}
