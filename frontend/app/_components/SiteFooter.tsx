"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import styles from "@/app/_components/SiteFooter.module.css";
import { isAdminPath, SITE_MENU } from "@/app/_components/siteMenu";

// 와이어프레임: 공통 · 푸터 (footer)
// 관리자 화면인지 알아야 해서 클라이언트 컴포넌트로 둔다.
// TODO: 운영 주체·연락처는 확정 후 기입한다.

const TAGLINE = "트렌드의 맥락을 발견하는 공간";

// 아직 화면이 없는 링크는 href 없이 글자만 보인다.
const COLUMNS: { title: string; links: { label: string; href?: string }[] }[] = [
  {
    title: "서비스",
    links: SITE_MENU.slice(0, 3).map((item) => ({
      label: item.label,
      href: item.href,
    })),
  },
  {
    title: "안내",
    // TODO: 서비스 소개, 공지사항, 문의하기 화면이 생기면 연결
    links: [{ label: "서비스 소개" }, { label: "공지사항" }, { label: "문의하기" }],
  },
  {
    title: "정책",
    // TODO: 정책 문서가 정해지면 연결
    links: [{ label: "이용약관" }, { label: "개인정보처리방침" }, { label: "운영정책" }],
  },
];

export default function SiteFooter() {
  const pathname = usePathname();

  if (isAdminPath(pathname)) {
    return null;
  }

  return (
    <>
      {/* 데스크톱: 로고와 3단 링크 */}
      <footer
        className={styles.desktop}
        style={{
          borderTop: "1px solid #999",
          background: "#f4f4f4",
          color: "#222",
        }}
      >
        <div style={{ padding: "32px 40px 18px" }}>
          <div style={{ display: "flex", flexWrap: "wrap", gap: 40 }}>
            <div style={{ flex: "1 1 180px" }}>
              <p style={{ fontSize: 16, fontWeight: 500 }}>trendizm</p>
              <p style={{ marginTop: 12, fontSize: 11, color: "#555" }}>{TAGLINE}</p>
            </div>
            {COLUMNS.map((column) => (
              <section key={column.title} style={{ flex: "0 1 132px" }}>
                <h2 style={{ fontSize: 15, fontWeight: 700 }}>{column.title}</h2>
                <ul
                  style={{
                    display: "flex",
                    flexDirection: "column",
                    gap: 8,
                    marginTop: 10,
                    listStyle: "none",
                    fontSize: 11,
                    color: "#555",
                  }}
                >
                  {column.links.map((link) => (
                    <li key={link.label}>
                      {link.href ? <Link href={link.href}>{link.label}</Link> : link.label}
                    </li>
                  ))}
                </ul>
              </section>
            ))}
          </div>
          <p
            style={{
              marginTop: 64,
              paddingTop: 16,
              borderTop: "1px solid #999",
              fontSize: 11,
              color: "#555",
            }}
          >
            © trendizm
          </p>
        </div>
      </footer>

      {/* 모바일: 로고, 소개, 묶음 링크를 세로로 쌓는다 */}
      <footer
        className={styles.mobile}
        style={{
          borderTop: "1px solid #999",
          background: "#f4f4f4",
          color: "#222",
        }}
      >
        <div style={{ padding: "16px 18px 18px" }}>
          <p style={{ fontSize: 16, fontWeight: 500 }}>trendizm</p>
          <p style={{ marginTop: 10, fontSize: 11, color: "#555" }}>{TAGLINE}</p>
          {/* TODO: 묶음별 안내 화면이 생기면 연결 */}
          <ul
            style={{
              display: "flex",
              gap: 10,
              marginTop: 16,
              paddingTop: 12,
              borderTop: "1px solid #999",
              listStyle: "none",
              fontSize: 12,
            }}
          >
            <li>서비스</li>
            <li>안내</li>
            <li>약관 및 정책</li>
          </ul>
          <p style={{ marginTop: 34, fontSize: 10, color: "#555" }}>© trendizm</p>
        </div>
      </footer>

      {/* 모바일에서만 하단 고정 내비 높이만큼 비운다. */}
      <div aria-hidden className={styles.bottomNavSpace} />
    </>
  );
}
