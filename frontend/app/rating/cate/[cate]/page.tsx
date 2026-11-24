import { notFound } from "next/navigation";
import { Suspense } from "react";
import RatingListSection from "@/app/rating/_components/RatingListSection";
import { findCategory } from "@/app/rating/_lib/categories";

// 대분류별 콘텐츠 목록. 예: /rating/cate/media
export default function RatingCategoryPage({
  params,
  searchParams,
}: PageProps<"/rating/cate/[cate]">) {
  return (
    <Suspense fallback={<p style={{ padding: 32 }}>불러오는 중...</p>}>
      <RatingCategoryList params={params} searchParams={searchParams} />
    </Suspense>
  );
}

async function RatingCategoryList({
  params,
  searchParams,
}: PageProps<"/rating/cate/[cate]">) {
  const { cate } = await params;
  if (!findCategory(cate)) {
    notFound();
  }

  return <RatingListSection cate={cate} searchParams={searchParams} />;
}
