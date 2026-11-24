// 순위 API 응답 형식. 백엔드 ranking 도메인의 application/dto(RankingTabs, RankingSnapshot 등)와 같다.
// 날짜는 시간대 없는 문자열이다. 예: "2026-11-20T18:00:00"

/** GET /api/rankings/tabs. 플랫폼은 정해진 순서, 카테고리는 미분류를 뺀 id 순이다 */
export type RankingTabs = {
  platforms: RankingPlatform[];
  categories: RankingCategory[];
};

/** 플랫폼 탭. slug는 주소(/ranking/[platform])와 순위 조회에 쓰고, label은 탭에 보여 준다. 예: youtube, YouTube */
export type RankingPlatform = {
  slug: string;
  label: string;
};

/** 카테고리 탭. 주소(/ranking/[platform]/[category])와 순위 조회(category=)에 slug를 쓴다 */
export type RankingCategory = {
  id: number;
  slug: string;
  title: string;
};

/**
 * GET /api/rankings?platform=instagram&category=challenge (둘 다 slug). 가장 최근 스냅샷의 1~10위.
 * categoryId는 카테고리 id다. 아직 수집 전이면 rankings는 빈 배열이고 시각은 null이다
 */
export type RankingSnapshot = {
  platform: string;
  categoryId: number;
  startedAt: string | null;
  collectedAt: string | null;
  rankings: Ranking[];
};

export type Ranking = {
  id: number;
  rank: number;
  keyword: string;
  /** AI 요약. 만들기 전이거나 만들지 못했으면 null */
  summary: string | null;
  /** 관련 링크 전부(표시 순서). 몇 개씩 보여 줄지는 화면에서 정한다 */
  links: RankingLink[];
};

export type RankingLink = {
  id: number;
  /** 게시물 제목(캡션). 줄바꿈이 들어 있을 수 있고, 없으면 null */
  title: string | null;
  url: string;
  /** 플랫폼 정책상 표시할 수 없으면 null */
  thumbnailUrl: string | null;
  description: string | null;
  publishedAt: string | null;
};
