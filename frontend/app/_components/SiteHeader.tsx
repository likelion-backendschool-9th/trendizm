"use client";

import Image from "next/image";
import Link from "next/link";
import { usePathname } from "next/navigation";
import styles from "@/app/_components/SiteHeader.module.css";
import { isAdminPath, isUnder, SITE_MENU } from "@/app/_components/siteMenu";

// 와이어프레임: 공통 · 헤더 (header)
// 현재 주소로 관리자 화면 여부와 선택된 메뉴를 정해야 해서 클라이언트 컴포넌트로 둔다.
// TODO: 로그인 후에는 로그인/회원가입 대신 계정 메뉴를 보인다.
export default function SiteHeader() {
  const pathname = usePathname();

  if (isAdminPath(pathname)) {
    return null;
  }

  return (
    <>
      {/* 데스크톱: 로고 + 핵심 메뉴 5개 + 로그인/회원가입 */}
      <header
        className={styles.desktop}
        style={{
          borderBottom: "1px solid #999",
          background: "#fff",
          color: "#111",
        }}
      >
        <div
          style={{
            display: "flex",
            alignItems: "center",
            gap: 24,
            height: 70,
            padding: "0 20px 0 40px",
          }}
        >
          <Link href="/" style={{ fontSize: 22, fontWeight: 500, letterSpacing: 0.5 }}>
            trendizm
          </Link>
          <nav aria-label="주요 메뉴" style={{ flex: 1 }}>
            <ul
              style={{
                display: "flex",
                justifyContent: "space-around",
                maxWidth: 520,
                listStyle: "none",
              }}
            >
              {SITE_MENU.map((item) => {
                const selected = isUnder(pathname, item.href);

                return (
                  <li key={item.href}>
                    <Link
                      href={item.href}
                      aria-current={selected ? "page" : undefined}
                      style={{
                        fontSize: 12,
                        fontWeight: selected ? 700 : 400,
                        whiteSpace: "nowrap",
                      }}
                    >
                      {item.label}
                    </Link>
                  </li>
                );
              })}
            </ul>
          </nav>
          <div style={{ display: "flex", alignItems: "center", gap: 28 }}>
            <Link href="/login" style={{ fontSize: 13 }}>
              로그인
            </Link>
            <Link
              href="/signup"
              style={{
                padding: "8px 8px",
                border: "1px solid #999",
                borderRadius: 3,
                fontSize: 12,
              }}
            >
              회원가입
            </Link>
          </div>
        </div>
      </header>

      {/* 모바일: 가운데 로고 + 오른쪽 로그인 */}
      <header
        className={styles.mobile}
        style={{
          borderBottom: "1px solid #999",
          background: "#fff",
          color: "#111",
        }}
      >
        <div
          style={{
            position: "relative",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            height: 58,
          }}
        >
          <Link href="/" style={{ fontSize: 18, fontWeight: 500 }}>
            trendizm
          </Link>
          <Link
            href="/login"
            style={{
              position: "absolute",
              right: 10,
              padding: "7px 12px",
              border: "1px solid #999",
              borderRadius: 3,
              fontSize: 12,
            }}
          >
            로그인
          </Link>
        </div>
      </header>

      {/* 모바일: 화면 하단에 고정한 핵심 메뉴 5개 */}
      <nav
        aria-label="주요 메뉴"
        className={styles.mobile}
        style={{
          position: "fixed",
          right: 0,
          bottom: 0,
          left: 0,
          zIndex: 10,
          paddingBottom: "env(safe-area-inset-bottom)",
          borderTop: "1px solid #999",
          background: "#fff",
        }}
      >
        <ul style={{ display: "flex", height: 64, listStyle: "none" }}>
          {SITE_MENU.map((item) => {
            const selected = isUnder(pathname, item.href);

            return (
              <li key={item.href} style={{ flex: 1 }}>
                <Link
                  href={item.href}
                  aria-current={selected ? "page" : undefined}
                  style={{
                    display: "flex",
                    flexDirection: "column",
                    alignItems: "center",
                    justifyContent: "center",
                    gap: 4,
                    height: "100%",
                    color: selected ? "#111" : "#777",
                    fontSize: 11,
                    fontWeight: selected ? 700 : 400,
                  }}
                >
                  <Image
                    src={item.icon}
                    alt=""
                    width={22}
                    height={22}
                    style={{
                      // 아이콘 선이 회색이라 선택된 메뉴만 짙게 바꾼다.
                      filter: selected ? "brightness(0.25)" : undefined,
                    }}
                  />
                  {item.short}
                </Link>
              </li>
            );
          })}
        </ul>
      </nav>
    </>
  );
}
