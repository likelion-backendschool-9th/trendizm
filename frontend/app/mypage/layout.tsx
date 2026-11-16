import MypageShell, {
  type MypageMenuItem,
} from "@/app/mypage/_components/MypageShell";

// 마이페이지 왼쪽 메뉴. 첫 번째 항목이 마이페이지 홈이다.
const MENU: MypageMenuItem[] = [
  {
    label: "내 활동",
    href: "/mypage",
    description: "내 프로필과 활동 내역을 한곳에서 관리합니다.",
  },
  {
    label: "저장한 콘텐츠",
    href: "/mypage/saved",
    description: "저장한 트렌드와 평점 콘텐츠를 모아봅니다.",
  },
  {
    label: "포인트 내역",
    href: "/mypage/points",
    description: "포인트를 충전하고 적립·사용 내역을 확인합니다.",
  },
  {
    label: "개인정보 설정",
    href: "/mypage/privacy",
    description: "계정 정보를 확인하고 개인정보를 관리합니다.",
  },
  {
    label: "클러스터 설정",
    href: "/mypage/clusters",
    description: "관심 있는 클러스터를 검색하고 피드에 반영할 목록을 정합니다.",
  },
];

export default function MypageLayout({ children }: LayoutProps<"/mypage">) {
  return <MypageShell menu={MENU}>{children}</MypageShell>;
}
