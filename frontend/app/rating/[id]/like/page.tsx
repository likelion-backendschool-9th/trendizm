import { Suspense } from "react";
import RatingDetailSection from "@/app/rating/_components/RatingDetailSection";

// 콘텐츠 상세의 좋아요 탭. 좋아요 후기 목록과 후기 작성 폼을 보여 준다.
export default function RatingLikePage({ params }: PageProps<"/rating/[id]/like">) {
  return (
    <Suspense fallback={<p style={{ padding: 32 }}>불러오는 중...</p>}>
      <RatingDetailSection params={params} sentiment="like" />
    </Suspense>
  );
}
