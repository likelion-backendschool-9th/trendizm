import { redirect } from "next/navigation";
import { Suspense } from "react";
import type { RankingTabs } from "@/app/ranking/_lib/types";
import { serverFetch } from "@/lib/api/server";

// /ranking은 첫 번째 플랫폼의 첫 번째 카테고리로 보낸다. 플랫폼·카테고리는 백엔드(GET /api/rankings/tabs)에서 받는다.
// 카테고리가 하나도 없으면 플랫폼 주소로 보내고, 거기서 빈 순위를 보여 준다.
export default function RankingPage() {
  return (
    <Suspense fallback={<p>불러오는 중...</p>}>
      <RedirectToFirstTab />
    </Suspense>
  );
}

// 항상 redirect하므로 그리는 것이 없다(redirect는 예외를 던져 돌아오지 않는다)
async function RedirectToFirstTab(): Promise<never> {
  const { platforms, categories } = await serverFetch<RankingTabs>("/api/rankings/tabs");
  const [platform] = platforms;
  const [category] = categories;

  redirect(category ? `/ranking/${platform.slug}/${category.slug}` : `/ranking/${platform.slug}`);
}
