// 호불호 평점 API 응답. 백엔드 rating.application.dto의 XxxResponse와 같은 모양이다.

/** slug는 주소와 요청에 쓰는 값, label은 화면 이름 */
export type RatingLabel = { slug: string; label: string };

/** 좋아요·싫어요 집계. 후기가 없으면 비율·평균은 null */
export type SentimentStats = {
  likeCount: number;
  dislikeCount: number;
  likeRatio: number | null;
  likeAverageScore: number | null;
  dislikeAverageScore: number | null;
};

/** GET /api/ratings 목록의 콘텐츠 하나 */
export type RatingContentSummary = {
  id: number;
  category: RatingLabel;
  subcategory: RatingLabel;
  status: RatingLabel;
  title: string;
  imageUrl: string | null;
  ageRating: number | null;
  releasedAt: string | null;
  stats: SentimentStats;
};

/** GET /api/ratings/{id} 콘텐츠 상세. extra 키는 RatingContentExtraKey (season, episodeLength) */
export type RatingContentDetail = RatingContentSummary & {
  description: string | null;
  extra: Record<string, unknown> | null;
  createdAt: string;
};

/** 후기 하나. mine·likedByMe는 로그인하지 않았으면 false */
export type RatingReviewItem = {
  id: number;
  contentId: number;
  sentiment: RatingLabel;
  score: number;
  title: string;
  body: string;
  progress: number | null;
  authorNickname: string;
  mine: boolean;
  likeCount: number;
  likedByMe: boolean;
  createdAt: string;
  updatedAt: string;
};

/** POST /api/ratings/{id}/reviews 응답. 처음 쓰면 created: true, rewardPoint: 10 */
export type RatingReviewSaveResult = {
  review: RatingReviewItem;
  created: boolean;
  rewardPoint: number;
};

export type Sentiment = "like" | "dislike";

/** 후기 정렬 (백엔드 ReviewSort slug) */
export const REVIEW_SORTS = [
  { slug: "latest", name: "최신순" },
  { slug: "likes", name: "공감순" },
  { slug: "score-high", name: "별점 높은 순" },
  { slug: "score-low", name: "별점 낮은 순" },
] as const;
export type ReviewSort = (typeof REVIEW_SORTS)[number]["slug"];
