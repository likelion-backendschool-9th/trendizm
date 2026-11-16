import Link from "next/link";
import { Suspense } from "react";
import OpinionCard, { type Opinion } from "@/app/lookup/_components/OpinionCard";

// 의견 상세. 와이어프레임에 프레임이 없어 밈 검색 - 결과의 의견 카드 한 장을 옮겼다.

const OPINION: Opinion = {
  id: 1,
  meaning: "A라는 뜻",
  author: "아무개1",
  agreeCount: 12,
  description:
    "A라는 뜻에 대한 설명이고 B라는 내용이 있으며 C를 통해 화제가 되었다.",
  examples: ["아 역시 ‘A’하죠~", "역시 ‘A’보단 ‘A’지~"],
  comments: [
    {
      id: 1,
      author: "댓글작성자 A",
      body: "댓글입니다 댓글입니다",
      createdAt: "2026-01-01 14:00:00",
      likeCount: 100,
      replies: [
        {
          id: 2,
          author: "댓글작성자 AB",
          body: "댓글입니다 댓글입니다",
          createdAt: "2026-01-01 14:00:00",
          likeCount: 80,
        },
      ],
    },
  ],
};

export default function OpinionDetailPage({
  params,
}: PageProps<"/lookup/[slug]/[id]">) {
  return (
    <Suspense fallback={<p>불러오는 중...</p>}>
      <OpinionDetail params={params} />
    </Suspense>
  );
}

async function OpinionDetail({
  params,
}: Pick<PageProps<"/lookup/[slug]/[id]">, "params">) {
  const { slug } = await params;

  return (
    <main
      style={{
        width: "100%",
        maxWidth: 800,
        margin: "0 auto",
        padding: "32px 28px 48px",
        background: "#fff",
        color: "#111",
      }}
    >
      {/* params에는 인코딩된 값이 그대로 들어온다. */}
      <h1 style={{ marginBottom: 24, fontSize: 24, fontWeight: 700 }}>
        {decodeURIComponent(slug)}
      </h1>
      <OpinionCard opinion={OPINION} />
      <Link
        href={`/lookup/${slug}`}
        style={{
          display: "block",
          width: 144,
          margin: "28px auto 0",
          padding: "12px 0",
          border: "1px solid #111",
          textAlign: "center",
          fontSize: 14,
        }}
      >
        목록으로
      </Link>
    </main>
  );
}
