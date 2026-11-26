import { notFound, redirect } from "next/navigation";
import { Suspense } from "react";
import RankingList from "@/app/ranking/_components/RankingList";
import type { RankingTabs } from "@/app/ranking/_lib/types";
import { serverFetch } from "@/lib/api/server";

// 플랫폼만 고른 주소는 그 플랫폼의 첫 번째 카테고리로 보낸다. 예: /ranking/youtube → /ranking/youtube/challenge
// 카테고리가 하나도 없으면(로컬 DB에 아직 넣지 않은 경우 등) 탭과 빈 순위를 보여 준다.
export default function RankingPlatformPage(
  { params } : PageProps<"/ranking/[platform]">
) {
  return (
    <Suspense fallback={<p>불러오는 중...</p>}>
      <RankingPlatform params={params} />
    </Suspense>
  );
}

async function RankingPlatform({
  params,
}: Pick<PageProps<"/ranking/[platform]">, "params">) {
  const { platform } = await params;
  const tabs = await serverFetch<RankingTabs>("/api/rankings/tabs");
  if (!tabs.platforms.some((item) => item.slug === platform)) {
    notFound();
  }

  const [category] = tabs.categories;
  if (category) {
    redirect(`/ranking/${platform}/${category.slug}`);
  }

  return <RankingList tabs={tabs} platform={platform} rankings={[]} />;
}
