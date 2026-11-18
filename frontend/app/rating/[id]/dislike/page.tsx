import { Suspense } from "react";
import RatingDetail from "@/app/rating/_components/RatingDetail";
import type { RatingReview } from "@/app/rating/_components/RatingList";

const CONTENT = {
  title: "콘텐츠 제목",
  ageRating: 12,
  status: "방영중",
  releasedAt: "2026.10.28",
  season: "시즌 2",
  episodeLength: "에피소드당 60분",
  category: { slug: "media", name: "미디어" },
  subcategory: { slug: "airing", name: "방영중" },
};

const LIKE = {
  count: 128,
  ratio: 72,
  averageScore: 4.0,
  tags: [
    { label: "연령", value: "20대" },
    { label: "MBTI", value: "ENFP" },
    { label: "취미", value: "사과깎기" },
    { label: "직군", value: "자택경비원" },
  ],
};

const DISLIKE = {
  count: 35,
  ratio: 28,
  averageScore: 3.0,
  tags: [
    { label: "연령", value: "30대" },
    { label: "MBTI", value: "I·J" },
    { label: "선호", value: "멜로" },
    { label: "취미", value: "요리" },
    { label: "직군", value: "학생" },
  ],
};

const REVIEWS: RatingReview[] = [
  {
    id: 3,
    title: "정말 별로네요",
    score: 3.0,
    watched: "2화까지 시청",
    body: "개별 사용자의 상세 후기가 이곳에 표시됩니다.",
    createdAt: "2020.10.10 23:11",
    likeCount: 0,
    author: { id: "ooq1024", nickname: "김원석", tags: ["30대", "INFJ", "멜로", "사무직"] },
  },
  {
    id: 4,
    title: "정말 별로네요",
    score: 3.0,
    watched: "2화까지 시청",
    body: "개별 사용자의 상세 후기가 이곳에 표시됩니다.",
    createdAt: "2020.10.10 23:11",
    likeCount: 0,
    author: { id: "p02iiah", nickname: "멜로장인", tags: ["30대", "ISTJ", "요리", "학생"] },
  },
];

export default function RatingDislikePage({
  params,
}: PageProps<"/rating/[id]/dislike">) {
  return (
    <Suspense fallback={<p>불러오는 중...</p>}>
      <RatingDislike params={params} />
    </Suspense>
  );
}

async function RatingDislike({
  params,
}: Pick<PageProps<"/rating/[id]/dislike">, "params">) {
  const { id } = await params;

  return (
    <RatingDetail
      type="dislike"
      contentId={id}
      content={CONTENT}
      like={LIKE}
      dislike={DISLIKE}
      reviews={REVIEWS}
    />
  );
}
