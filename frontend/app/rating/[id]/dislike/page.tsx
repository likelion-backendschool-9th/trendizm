import { Suspense } from "react";
import RatingDetailSection from "@/app/rating/_components/RatingDetailSection";

// 콘텐츠 상세의 싫어요 탭. 싫어요 후기 목록과 후기 작성 폼을 보여 준다.
export default function RatingDislikePage({ params }: PageProps<"/rating/[id]/dislike">) {
  return (
    <Suspense fallback={<p style={{ padding: 32 }}>불러오는 중...</p>}>
      <RatingDetailSection params={params} sentiment="dislike" />
    </Suspense>
  );
}
