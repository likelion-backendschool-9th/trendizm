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
    id: 1,
    title: "정말 짱이네요",
    score: 4.0,
    watched: "2화까지 시청",
    body: "개별 사용자의 상세 후기가 이곳에 표시됩니다.",
    createdAt: "2020.10.10 23:11",
    likeCount: 0,
    author: { id: "dkdlel", nickname: "영화마니아", tags: ["20대", "ENFP", "여행다니기", "사무직"] },
  },
  {
    id: 2,
    title: "정말 짱이네요",
    score: 4.0,
    watched: "2화까지 시청",
    body: "개별 사용자의 상세 후기가 이곳에 표시됩니다.",
    createdAt: "2020.10.10 23:11",
    likeCount: 0,
    author: { id: "dkdlel", nickname: "영화마니아", tags: ["20대", "ENFP", "여행다니기", "사무직"] },
  },
];

export default function RatingLikePage({
  params,
}: PageProps<"/rating/[id]/like">) {
  return (
    <Suspense fallback={<p>불러오는 중...</p>}>
      <RatingLike params={params} />
    </Suspense>
  );
}

async function RatingLike({
  params,
}: Pick<PageProps<"/rating/[id]/like">, "params">) {
  const { id } = await params;

  return (
    <RatingDetail
      type="like"
      contentId={id}
      content={CONTENT}
      like={LIKE}
      dislike={DISLIKE}
      reviews={REVIEWS}
    />
  );
}
