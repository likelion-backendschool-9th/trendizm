"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import type { ReactNode } from "react";

export type MypageMenuItem = {
  label: string;
  href: string;
  // 경로 아래에 보이는 화면 설명
  description: string;
};

type MypageShellProps = {
  menu: MypageMenuItem[];
  children: ReactNode;
};

// 마이페이지 공통 틀: 경로, 화면 설명, 왼쪽 메뉴.
// 현재 주소로 경로와 선택된 메뉴를 정해야 해서 클라이언트 컴포넌트로 둔다.
export default function MypageShell({ menu, children }: MypageShellProps) {
  const pathname = usePathname();
  const current = menu.find((item) => item.href === pathname) ?? menu[0];
  const [home] = menu;

  return (
    <div
      style={{
        width: "100%",
        maxWidth: 1076,
        margin: "0 auto",
        padding: "36px 48px 48px",
        background: "#fff",
        color: "#111",
      }}
    >
      <nav aria-label="경로" style={{ fontSize: 14, color: "#555" }}>
        <Link href="/">홈</Link>
        <span style={{ margin: "0 8px" }}>/</span>
        <Link href={home.href}>마이페이지</Link>
        {current !== home && (
          <>
            <span style={{ margin: "0 8px" }}>/</span>
            {current.label}
          </>
        )}
      </nav>
      <p style={{ marginTop: 18, fontSize: 14, color: "#555" }}>
        {current.description}
      </p>

      <div
        style={{
          display: "flex",
          flexWrap: "wrap",
          alignItems: "stretch",
          gap: 24,
          marginTop: 32,
        }}
      >
        <nav
          aria-label="마이페이지 메뉴"
          style={{ flex: "0 0 184px", border: "1px solid #999" }}
        >
          <ul style={{ listStyle: "none" }}>
            {menu.map((item) => {
              const selected = item === current;

              return (
                <li key={item.href}>
                  <Link
                    href={item.href}
                    aria-current={selected ? "page" : undefined}
                    style={{
                      display: "block",
                      padding: "18px 20px",
                      background: selected ? "#f2f2f2" : "transparent",
                      color: selected ? "#111" : "#555",
                      fontSize: 14,
                      fontWeight: selected ? 700 : 400,
                    }}
                  >
                    {item.label}
                  </Link>
                </li>
              );
            })}
            <li>
              {/* TODO: 로그아웃 요청 연결 */}
              <button
                type="button"
                style={{
                  width: "100%",
                  padding: "18px 20px",
                  border: "none",
                  background: "none",
                  color: "#555",
                  fontSize: 14,
                  textAlign: "left",
                  cursor: "pointer",
                }}
              >
                로그아웃
              </button>
            </li>
          </ul>
        </nav>

        <div style={{ flex: "1 1 560px", minWidth: 0 }}>{children}</div>
      </div>
    </div>
  );
}
