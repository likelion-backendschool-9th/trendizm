import { notFound } from "next/navigation";
import { Suspense } from "react";
import RatingListSection from "@/app/rating/_components/RatingListSection";
import { findCategory, findSubcategory } from "@/app/rating/_lib/categories";

// 소분류별 콘텐츠 목록. 예: /rating/cate/media/drama
export default function RatingSubcategoryPage({
  params,
  searchParams,
}: PageProps<"/rating/cate/[cate]/[subcate]">) {
  return (
    <Suspense fallback={<p style={{ padding: 32 }}>불러오는 중...</p>}>
      <RatingSubcategoryList params={params} searchParams={searchParams} />
    </Suspense>
  );
}

async function RatingSubcategoryList({
  params,
  searchParams,
}: PageProps<"/rating/cate/[cate]/[subcate]">) {
  const { cate, subcate } = await params;
  const category = findCategory(cate);
  // 소분류가 다른 대분류에 속하면(예: /rating/cate/media/cafe) 없는 주소로 본다
  if (!category || !findSubcategory(category, subcate)) {
    notFound();
  }

  return <RatingListSection cate={cate} subcate={subcate} searchParams={searchParams} />;
}
