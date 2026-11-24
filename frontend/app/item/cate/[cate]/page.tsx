import { notFound } from "next/navigation";
import { Suspense } from "react";
import ItemListSection from "@/app/item/_components/ItemListSection";
import { findCategory } from "@/app/item/_lib/categories";

// 대분류별 질문 글 목록. 예: /item/cate/fashion
export default function ItemCategoryPage({
  params,
  searchParams,
}: PageProps<"/item/cate/[cate]">) {
  return (
    <Suspense fallback={<p style={{ padding: 32 }}>불러오는 중...</p>}>
      <ItemCategoryList params={params} searchParams={searchParams} />
    </Suspense>
  );
}

async function ItemCategoryList({
  params,
  searchParams,
}: PageProps<"/item/cate/[cate]">) {
  const { cate } = await params;
  if (!findCategory(cate)) {
    notFound();
  }

  return <ItemListSection cate={cate} searchParams={searchParams} />;
}
