import { Suspense } from "react";
import ItemListSection from "@/app/item/_components/ItemListSection";

// 전체 질문 글 목록. ?page=로 페이지를 고른다(1부터).
export default function ItemPage({ searchParams }: PageProps<"/item">) {
  return (
    <Suspense fallback={<p style={{ padding: 32 }}>불러오는 중...</p>}>
      <ItemListSection searchParams={searchParams} />
    </Suspense>
  );
}
