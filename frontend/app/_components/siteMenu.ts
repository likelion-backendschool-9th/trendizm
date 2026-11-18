// 헤더와 푸터가 함께 쓰는 서비스 메뉴와 경로 판별.

export const SITE_MENU = [
  { label: "밈 검색", short: "검색", href: "/lookup", icon: "/images/nav/search.svg" },
  { label: "플랫폼 트렌드", short: "트렌드", href: "/ranking", icon: "/images/nav/trend.svg" },
  { label: "트렌드 피드", short: "피드", href: "/feed", icon: "/images/nav/feed.svg" },
  { label: "호·불호 평점", short: "평점", href: "/rating", icon: "/images/nav/rating.svg" },
  { label: "트렌드 아이템", short: "아이템", href: "/item", icon: "/images/nav/item.svg" },
];

// href 자신이거나 그 하위 경로면 true
export function isUnder(pathname: string, href: string) {
  return pathname === href || pathname.startsWith(`${href}/`);
}

// 관리자 화면에서는 공통 헤더와 푸터를 보이지 않는다.
export function isAdminPath(pathname: string) {
  return isUnder(pathname, "/admin");
}
