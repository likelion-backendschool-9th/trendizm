import { notFound } from "next/navigation";
import { Suspense } from "react";
import RankingList from "@/app/ranking/_components/RankingList";
import type { RankingSnapshot, RankingTabs } from "@/app/ranking/_lib/types";
import { serverFetch } from "@/lib/api/server";
import { ApiError } from "@/lib/api/types";

// 플랫폼·카테고리의 가장 최근 순위. 예: /ranking/instagram/challenge
// 주소의 두 slug를 그대로 순위 API(GET /api/rankings?platform=instagram&category=challenge)에 보낸다.
// 탭 목록과 순위는 서로 기다릴 필요가 없어 함께 부른다.
export default function RankingCategoryPage({
  params,
}: PageProps<"/ranking/[platform]/[category]">) {
  return (
    <Suspense fallback={<p>불러오는 중...</p>}>
      <RankingCategory params={params} />
    </Suspense>
  );
}

async function RankingCategory({
  params,
}: Pick<PageProps<"/ranking/[platform]/[category]">, "params">) {
  const { platform, category } = await params;
  const [tabs, snapshot] = await Promise.all([
    serverFetch<RankingTabs>("/api/rankings/tabs"),
    fetchSnapshot(platform, category),
  ]);

  return (
    <RankingList
      tabs={tabs}
      platform={platform}
      category={category}
      rankings={snapshot.rankings}
      collectedAt={snapshot.collectedAt}
    />
  );
}

// 없는 플랫폼·카테고리(미분류 포함)는 백엔드가 404로 알려 주므로 404 화면을 보여 준다.
async function fetchSnapshot(platform: string, category: string): Promise<RankingSnapshot> {
  const query = new URLSearchParams({ platform, category });
  try {
    return await serverFetch<RankingSnapshot>(`/api/rankings?${query}`);
  } catch (error) {
    if (
      error instanceof ApiError &&
      (error.code === "RANKING_PLATFORM_NOT_FOUND" || error.code === "RANKING_CATEGORY_NOT_FOUND")
    ) {
      notFound();
    }
    throw error;
  }
}
