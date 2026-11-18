import { Suspense } from "react";
import RankingList, {
  type Ranking,
  type RankingFilter,
} from "@/app/ranking/_components/RankingList";

const PLATFORMS: RankingFilter[] = [
  { slug: "youtube", name: "YouTube" },
  { slug: "instagram", name: "Instagram" },
  { slug: "threads", name: "Threads" },
  { slug: "x", name: "X" },
  { slug: "tiktok", name: "TikTok" },
];

const CATEGORIES: RankingFilter[] = [
  { slug: "sports", name: "운동/스포츠" },
  { slug: "food", name: "맛집/음식" },
  { slug: "pet", name: "반려동물" },
  { slug: "finance", name: "재테크/투자" },
  { slug: "family", name: "육아/가족" },
  { slug: "ai", name: "AI" },
  { slug: "travel", name: "여행/여가" },
];

const RANKINGS: Ranking[] = Array.from({ length: 10 }, (_, index) => ({
  rank: index + 1,
  keyword: `키워드 ${index + 1}`,
  summary: `키워드 ${index + 1}은 어떤 사람이 어떤 일을 하는 것을 계기로 사람들에게 전파되었다. 그 뒤 여러 커뮤니티에서 패러디가 이어지며 화제가 되었다.`,
  links: [1, 2, 3].map((id) => ({
    id,
    title: "제목",
    date: "2026.11.0" + id,
    description: "설명설명설명설명설명설명설명설명설명설명",
  })),
}));

export default function RankingPlatformPage({
  params,
}: PageProps<"/ranking/[platform]">) {
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

  return (
    <RankingList
      platforms={PLATFORMS}
      categories={CATEGORIES}
      platform={platform}
      rankings={RANKINGS}
    />
  );
}
