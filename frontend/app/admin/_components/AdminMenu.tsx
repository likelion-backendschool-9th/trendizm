"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

// 와이어프레임: 관리자 공통 메뉴 (wireframe)
// 대시보드(/admin)는 메뉴에 넣지 않는다.
const MENU = [
  { label: "밈 등록 관리", href: "/admin/memes" },
  { label: "회원 관리", href: "/admin/members" },
  { label: "트렌드 아이템", href: "/admin/items" },
  { label: "호불호 평점 콘텐츠", href: "/admin/ratings" },
  { label: "피드 리스트", href: "/admin/feeds" },
];

// 현재 주소로 선택된 메뉴를 정해야 해서 클라이언트 컴포넌트로 둔다.
export default function AdminMenu() {
  const pathname = usePathname();

  return (
    <nav
      aria-label="관리자 메뉴"
      style={{
        flex: "0 0 184px",
        alignSelf: "flex-start",
        minHeight: 404,
        padding: "16px 12px",
        border: "1px solid #999",
      }}
    >
      <h2 style={{ fontSize: 15, fontWeight: 700 }}>관리자 메뉴</h2>
      <ul
        style={{
          display: "flex",
          flexDirection: "column",
          gap: 4,
          marginTop: 4,
          listStyle: "none",
        }}
      >
        {MENU.map((item) => {
          // 하위 경로(예: /admin/memes/1)에서도 해당 메뉴를 선택된 것으로 본다.
          const selected =
            pathname === item.href || pathname.startsWith(`${item.href}/`);

          return (
            <li key={item.href}>
              <Link
                href={item.href}
                aria-current={selected ? "page" : undefined}
                style={{
                  display: "block",
                  height: 42,
                  padding: "10px 12px",
                  border: selected ? "1px solid #111" : "1px solid #999",
                  background: selected ? "#111" : "#fff",
                  color: selected ? "#fff" : "#222",
                  fontSize: 13,
                  fontWeight: selected ? 700 : 400,
                }}
              >
                {item.label}
              </Link>
            </li>
          );
        })}
      </ul>
    </nav>
  );
}
