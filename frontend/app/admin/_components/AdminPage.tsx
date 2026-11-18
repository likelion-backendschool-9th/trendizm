import Link from "next/link";
import type { ReactNode } from "react";
import AdminMenu from "@/app/admin/_components/AdminMenu";

type AdminPageProps = {
  title: string;
  description: string;
  // 경로 마지막 칸. 없으면 '홈 / 관리자'까지만 보인다.
  current?: string;
  children: ReactNode;
};

// 관리자 화면 공통 틀: 경로, 제목, 설명, 왼쪽 관리자 메뉴, 오른쪽 내용 상자.
export default function AdminPage({
  title,
  description,
  current,
  children,
}: AdminPageProps) {
  return (
    <main
      style={{
        width: "100%",
        maxWidth: 1076,
        margin: "0 auto",
        padding: "30px 48px 48px",
        background: "#fff",
        color: "#222",
      }}
    >
      <nav aria-label="경로" style={{ fontSize: 12, color: "#555" }}>
        <Link href="/">홈</Link>
        <span style={{ margin: "0 6px" }}>/</span>
        {current ? <Link href="/admin">관리자</Link> : "관리자"}
        {current && (
          <>
            <span style={{ margin: "0 6px" }}>/</span>
            {current}
          </>
        )}
      </nav>
      <h1 style={{ marginTop: 18, fontSize: 28, fontWeight: 700 }}>{title}</h1>
      <p style={{ marginTop: 14, fontSize: 13, color: "#555" }}>{description}</p>

      <div
        style={{
          display: "flex",
          flexWrap: "wrap",
          gap: 24,
          marginTop: 30,
        }}
      >
        <AdminMenu />
        <div
          style={{
            flex: "1 1 600px",
            minWidth: 0,
            padding: "20px 24px 18px",
            border: "1px solid #999",
          }}
        >
          {children}
        </div>
      </div>
    </main>
  );
}
