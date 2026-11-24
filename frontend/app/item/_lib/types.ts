// 트렌드 아이템 API 응답. 백엔드 item.application.dto의 XxxResponse와 같은 모양이다.

/** 대분류·소분류. slug는 주소와 요청에 쓰는 값, label은 화면 이름 */
export type ItemCategoryInfo = { slug: string; label: string };

/** GET /api/items 목록의 질문 글 하나 */
export type ItemQuestionSummary = {
  id: number;
  category: ItemCategoryInfo;
  subcategory: ItemCategoryInfo;
  title: string;
  contentPreview: string;
  authorNickname: string;
  commentCount: number;
  recommendationCount: number;
  createdAt: string;
};

/** GET /api/items/{id} 질문 글 상세 */
export type ItemQuestion = {
  id: number;
  category: ItemCategoryInfo;
  subcategory: ItemCategoryInfo;
  title: string;
  content: string;
  authorNickname: string;
  /** 보는 사람이 쓴 글인지 (로그인하지 않았으면 false) */
  mine: boolean;
  commentCount: number;
  recommendationCount: number;
  createdAt: string;
};

/** 댓글. 대댓글은 replies에 오래된 순으로 들어 있다 */
export type ItemComment = {
  id: number;
  parentId: number | null;
  content: string;
  authorNickname: string;
  likeCount: number;
  likedByMe: boolean;
  createdAt: string;
  replies: ItemComment[];
};

/**
 * 추천 아이템. 잠겨 있으면(locked) 이름·판매처·가격·태그·설명·이미지·링크가 null이다.
 * 내가 등록한 아이템과 이미 연 아이템은 열린 상태로 온다.
 */
export type ItemRecommendation = {
  id: number;
  questionId: number;
  authorNickname: string;
  mine: boolean;
  locked: boolean;
  title: string | null;
  seller: string | null;
  price: number | null;
  tags: string[] | null;
  description: string | null;
  imageUrl: string | null;
  linkUrl: string | null;
  targetAgeGroup: string | null;
  targetGender: string | null;
  unlockPrice: number;
  createdAt: string;
};
