import { notFound } from "next/navigation";
import { Suspense } from "react";
import ItemListSection from "@/app/item/_components/ItemListSection";
import { findCategory, findSubcategory } from "@/app/item/_lib/categories";

// 소분류별 질문 글 목록. 예: /item/cate/fashion/eyewear-accessories
export default function ItemSubcategoryPage({
  params,
  searchParams,
}: PageProps<"/item/cate/[cate]/[subcate]">) {
  return (
    <Suspense fallback={<p style={{ padding: 32 }}>불러오는 중...</p>}>
      <ItemSubcategoryList params={params} searchParams={searchParams} />
    </Suspense>
  );
}

async function ItemSubcategoryList({
  params,
  searchParams,
}: PageProps<"/item/cate/[cate]/[subcate]">) {
  const { cate, subcate } = await params;
  const category = findCategory(cate);
  // 소분류가 다른 대분류에 속하면(예: /item/cate/fashion/skincare) 없는 주소로 본다
  if (!category || !findSubcategory(category, subcate)) {
    notFound();
  }

  return <ItemListSection cate={cate} subcate={subcate} searchParams={searchParams} />;
}
