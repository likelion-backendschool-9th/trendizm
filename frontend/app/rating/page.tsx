import { Suspense } from "react";
import RatingListSection from "@/app/rating/_components/RatingListSection";

// 전체 콘텐츠 목록. ?status=로 진행 상태, ?page=로 페이지를 고른다(1부터).
export default function RatingPage({ searchParams }: PageProps<"/rating">) {
  return (
    <Suspense fallback={<p style={{ padding: 32 }}>불러오는 중...</p>}>
      <RatingListSection searchParams={searchParams} />
    </Suspense>
  );
}
