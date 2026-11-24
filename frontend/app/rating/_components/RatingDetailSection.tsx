import { notFound } from "next/navigation";
import RatingDetail from "@/app/rating/_components/RatingDetail";
import { fetchAsViewer } from "@/app/rating/_lib/fetchAsViewer";
import type { RatingContentDetail, RatingReviewItem, Sentiment } from "@/app/rating/_lib/types";
import { serverFetch } from "@/lib/api/server";
import { ApiError, type PageResponse } from "@/lib/api/types";

export const REVIEW_PAGE_SIZE = 10;

type RatingDetailSectionProps = {
  params: Promise<{ id: string }>;
  sentiment: Sentiment;
};

// 콘텐츠 상세와, 고른 쪽(좋아요·싫어요) 후기 첫 페이지(최신순)를 불러온다.
// /rating/[id]/like와 /rating/[id]/dislike가 <Suspense> 안에서 같이 쓴다.
// 없거나 숨긴 콘텐츠면 백엔드가 404를 주므로 404 화면을 보여 준다.
export default async function RatingDetailSection({ params, sentiment }: RatingDetailSectionProps) {
  const { id } = await params;
  if (!/^\d+$/.test(id)) {
    notFound();
  }

  let content: RatingContentDetail;
  try {
    content = await serverFetch<RatingContentDetail>(`/api/ratings/${id}`);
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) {
      notFound();
    }
    throw error;
  }

  const reviews = await fetchAsViewer<PageResponse<RatingReviewItem>>(
    `/api/ratings/${id}/reviews?sentiment=${sentiment}&sort=latest&page=0&size=${REVIEW_PAGE_SIZE}`,
  );

  return <RatingDetail content={content} sentiment={sentiment} reviews={reviews} />;
}
