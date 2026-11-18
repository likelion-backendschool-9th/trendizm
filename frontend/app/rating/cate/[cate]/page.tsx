import { Suspense } from "react";
import RatingList, {
  type RatingCategory,
  type RatingContent,
} from "@/app/rating/_components/RatingList";

const CATEGORIES: RatingCategory[] = [
  {
    slug: "media",
    name: "미디어",
    icon: "/images/rating/media.svg",
    subcategories: [
      { slug: "airing", name: "방영중" },
      { slug: "ended", name: "완결" },
    ],
  },
  {
    slug: "food",
    name: "푸드",
    icon: "/images/rating/food.svg",
    subcategories: [
      { slug: "new", name: "신상" },
      { slug: "discontinued", name: "판매종료" },
    ],
  },
  {
    slug: "music",
    name: "음악",
    icon: "/images/rating/music.svg",
    subcategories: [
      { slug: "new-release", name: "신곡" },
      { slug: "upcoming-concert", name: "공연 예정" },
    ],
  },
  {
    slug: "books",
    name: "도서·웹툰",
    icon: "/images/rating/books.svg",
    subcategories: [
      { slug: "ongoing", name: "연재중" },
      { slug: "completed", name: "완결" },
    ],
  },
  {
    slug: "game",
    name: "게임",
    icon: "/images/rating/game.svg",
    subcategories: [
      { slug: "upcoming", name: "출시 예정" },
      { slug: "live", name: "서비스중" },
    ],
  },
  {
    slug: "place",
    name: "장소",
    icon: "/images/rating/place.svg",
    subcategories: [
      { slug: "ongoing", name: "진행중" },
      { slug: "ended", name: "종료" },
    ],
  },
];

const TOTAL_COUNT = 3;

const CONTENTS: RatingContent[] = [
  {
    id: 1,
    title: "컨텐츠 제목",
    category: "미디어",
    subcategory: "드라마",
    ageRating: 19,
    status: "방영중",
    releasedAt: "2026.11.08",
    season: "시즌 1",
    episodeLength: "에피소드당 50분",
    like: {
      count: 128,
      ratio: 72,
      averageScore: 4.0,
      tags: [
        { label: "연령", value: "20대" },
        { label: "MBTI", value: "ENFP" },
        { label: "BMI", value: "BMI 22.0" },
        { label: "취미", value: "사과깎기" },
        { label: "직군", value: "자택경비원" },
      ],
      reviews: [
        {
          id: 11,
          title: "정말 짱이네요",
          score: 4.0,
          watched: "2회까지 시청",
          body: "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
          createdAt: "2020.10.10 23:11:00",
          likeCount: 0,
          author: { id: "dkdlel", nickname: "영화마니아", tags: ["20대", "ENFP", "여행다니기", "사무직"] },
        },
        {
          id: 12,
          title: "정말 짱이네요",
          score: 4.0,
          watched: "2회까지 시청",
          body: "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
          createdAt: "2020.10.10 23:11:00",
          likeCount: 0,
          author: { id: "dkdlel", nickname: "영화마니아", tags: ["20대", "ENFP", "여행다니기", "사무직"] },
        },
      ],
    },
    dislike: {
      count: 35,
      ratio: 28,
      averageScore: 3.0,
      tags: [
        { label: "연령", value: "30대" },
        { label: "MBTI", value: "I · J" },
        { label: "선호", value: "멜로" },
        { label: "취미", value: "요리" },
        { label: "직군", value: "학생" },
      ],
      reviews: [
        {
          id: 13,
          title: "정말 별로네요",
          score: 3.0,
          watched: "2회까지 시청",
          body: "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
          createdAt: "2020.10.10 23:11:00",
          likeCount: 0,
          author: { id: "ooq1024", nickname: "김원석", tags: ["30대", "INFJ", "멜로", "사무직"] },
        },
        {
          id: 14,
          title: "정말 별로네요",
          score: 3.0,
          watched: "2회까지 시청",
          body: "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
          createdAt: "2020.10.10 23:11:00",
          likeCount: 0,
          author: { id: "p02iiah", nickname: "멜로장인", tags: ["30대", "ISTJ", "요리", "학생"] },
        },
      ],
    },
  },
  {
    id: 2,
    title: "컨텐츠 제목",
    category: "미디어",
    subcategory: "드라마",
    ageRating: 15,
    status: "방영중",
    releasedAt: "2026.11.04",
    season: "시즌 3",
    episodeLength: "에피소드당 60분",
    like: {
      count: 128,
      ratio: 72,
      averageScore: 4.0,
      tags: [
        { label: "연령", value: "20대" },
        { label: "MBTI", value: "ENFP" },
        { label: "BMI", value: "BMI 22.0" },
        { label: "취미", value: "사과깎기" },
        { label: "직군", value: "자택경비원" },
      ],
      reviews: [
        {
          id: 21,
          title: "정말 짱이네요",
          score: 4.0,
          watched: "2회까지 시청",
          body: "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
          createdAt: "2020.10.10 23:11:00",
          likeCount: 0,
          author: { id: "dkdlel", nickname: "영화마니아", tags: ["20대", "ENFP", "여행다니기", "사무직"] },
        },
        {
          id: 22,
          title: "정말 짱이네요",
          score: 4.0,
          watched: "2회까지 시청",
          body: "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
          createdAt: "2020.10.10 23:11:00",
          likeCount: 0,
          author: { id: "dkdlel", nickname: "영화마니아", tags: ["20대", "ENFP", "여행다니기", "사무직"] },
        },
      ],
    },
    dislike: {
      count: 35,
      ratio: 28,
      averageScore: 3.0,
      tags: [
        { label: "연령", value: "30대" },
        { label: "MBTI", value: "I · J" },
        { label: "선호", value: "멜로" },
        { label: "취미", value: "요리" },
        { label: "직군", value: "학생" },
      ],
      reviews: [
        {
          id: 23,
          title: "정말 별로네요",
          score: 3.0,
          watched: "2회까지 시청",
          body: "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
          createdAt: "2020.10.10 23:11:00",
          likeCount: 0,
          author: { id: "ooq1024", nickname: "김원석", tags: ["30대", "INFJ", "멜로", "사무직"] },
        },
        {
          id: 24,
          title: "정말 별로네요",
          score: 3.0,
          watched: "2회까지 시청",
          body: "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
          createdAt: "2020.10.10 23:11:00",
          likeCount: 0,
          author: { id: "p02iiah", nickname: "멜로장인", tags: ["30대", "ISTJ", "요리", "학생"] },
        },
      ],
    },
  },
  {
    id: 3,
    title: "컨텐츠 제목",
    category: "미디어",
    subcategory: "드라마",
    ageRating: 12,
    status: "방영중",
    releasedAt: "2026.10.28",
    season: "시즌 2",
    episodeLength: "에피소드당 50분",
    like: {
      count: 128,
      ratio: 72,
      averageScore: 4.0,
      tags: [
        { label: "연령", value: "20대" },
        { label: "MBTI", value: "ENFP" },
        { label: "BMI", value: "BMI 22.0" },
        { label: "취미", value: "사과깎기" },
        { label: "직군", value: "자택경비원" },
      ],
      reviews: [
        {
          id: 31,
          title: "정말 짱이네요",
          score: 4.0,
          watched: "2회까지 시청",
          body: "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
          createdAt: "2020.10.10 23:11:00",
          likeCount: 0,
          author: { id: "dkdlel", nickname: "영화마니아", tags: ["20대", "ENFP", "여행다니기", "사무직"] },
        },
        {
          id: 32,
          title: "정말 짱이네요",
          score: 4.0,
          watched: "2회까지 시청",
          body: "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
          createdAt: "2020.10.10 23:11:00",
          likeCount: 0,
          author: { id: "dkdlel", nickname: "영화마니아", tags: ["20대", "ENFP", "여행다니기", "사무직"] },
        },
      ],
    },
    dislike: {
      count: 35,
      ratio: 28,
      averageScore: 3.0,
      tags: [
        { label: "연령", value: "30대" },
        { label: "MBTI", value: "I · J" },
        { label: "선호", value: "멜로" },
        { label: "취미", value: "요리" },
        { label: "직군", value: "학생" },
      ],
      reviews: [
        {
          id: 33,
          title: "정말 별로네요",
          score: 3.0,
          watched: "2회까지 시청",
          body: "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
          createdAt: "2020.10.10 23:11:00",
          likeCount: 0,
          author: { id: "ooq1024", nickname: "김원석", tags: ["30대", "INFJ", "멜로", "사무직"] },
        },
        {
          id: 34,
          title: "정말 별로네요",
          score: 3.0,
          watched: "2회까지 시청",
          body: "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
          createdAt: "2020.10.10 23:11:00",
          likeCount: 0,
          author: { id: "p02iiah", nickname: "멜로장인", tags: ["30대", "ISTJ", "요리", "학생"] },
        },
      ],
    },
  },
];

export default function RatingCategoryPage({
  params,
}: PageProps<"/rating/cate/[cate]">) {
  return (
    <Suspense fallback={<p>불러오는 중...</p>}>
      <RatingCategoryList params={params} />
    </Suspense>
  );
}

async function RatingCategoryList({
  params,
}: Pick<PageProps<"/rating/cate/[cate]">, "params">) {
  const { cate } = await params;

  return (
    <RatingList
      categories={CATEGORIES}
      cate={cate}
      totalCount={TOTAL_COUNT}
      contents={CONTENTS}
    />
  );
}
