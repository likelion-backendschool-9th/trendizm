import { notFound } from "next/navigation";
import RatingList from "@/app/rating/_components/RatingList";
import { isRatingStatus } from "@/app/rating/_lib/categories";
import type { RatingContentSummary } from "@/app/rating/_lib/types";
import { serverFetch } from "@/lib/api/server";
import { ApiError, toApiPage, type PageResponse } from "@/lib/api/types";

const PAGE_SIZE = 10;

type RatingListSectionProps = {
  cate?: string;
  subcate?: string;
  searchParams: Promise<Record<string, string | string[] | undefined>>;
};

const first = (value: string | string[] | undefined) => (Array.isArray(value) ? value[0] : value);

// 콘텐츠 목록을 불러와 보여 준다. 세 목록 페이지가 <Suspense> 안에서 같이 쓴다.
// ?status=는 upcoming·ongoing·ended만 쓰고, 다른 값이면 무시한다(전체).
export default async function RatingListSection({
  cate,
  subcate,
  searchParams,
}: RatingListSectionProps) {
  const params = await searchParams;
  const status = first(params.status);
  const query = new URLSearchParams({
    page: String(toApiPage(first(params.page))),
    size: String(PAGE_SIZE),
  });
  if (cate) {
    query.set("category", cate);
  }
  if (subcate) {
    query.set("subcategory", subcate);
  }
  if (isRatingStatus(status)) {
    query.set("status", status);
  }

  let contents: PageResponse<RatingContentSummary>;
  try {
    contents = await serverFetch<PageResponse<RatingContentSummary>>(`/api/ratings?${query}`);
  } catch (error) {
    if (error instanceof ApiError && error.code === "RATING_INVALID_CATEGORY") {
      notFound();
    }
    throw error;
  }

  return (
    <RatingList
      cate={cate}
      subcate={subcate}
      status={isRatingStatus(status) ? status : undefined}
      contents={contents}
    />
  );
}
