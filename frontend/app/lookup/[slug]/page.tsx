import { Suspense } from "react";
import OpinionCard, { type Opinion } from "@/app/lookup/_components/OpinionCard";
import Pagination from "@/components/Pagination";

// 와이어프레임: 밈 검색 - 결과

const CONTENT = {
  title: "컨텐츠 제목",
  siteUrl: "video.example.com/watch/01",
};

const OPINION_COUNT = 10;

const OPINIONS: Opinion[] = [
  {
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
  },
  {
    id: 2,
    meaning: "B가 A해서 C라는 뜻",
    author: "아무개2",
    agreeCount: 5,
    description:
      "B가 A해서 C라는 뜻, 이것에 대한 어원은 ○○라는 커뮤니티에서 시작되었다.",
    examples: ["“000?” “000!”", "000급이네"],
    comments: [],
  },
];

export default function LookupResultPage({
  params,
}: PageProps<"/lookup/[slug]">) {
  return (
    <Suspense fallback={<p>불러오는 중...</p>}>
      <LookupResult params={params} />
    </Suspense>
  );
}

async function LookupResult({
  params,
}: Pick<PageProps<"/lookup/[slug]">, "params">) {
  const { slug } = await params;
  // params에는 인코딩된 값이 그대로 들어온다.
  const word = decodeURIComponent(slug);

  const info = [
    { label: "컨텐츠 제목", value: CONTENT.title },
    { label: "사이트 주소", value: CONTENT.siteUrl },
    { label: "확인이 필요한 단어", value: word },
  ];

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
      <section
        aria-label="콘텐츠 정보"
        style={{ display: "flex", flexWrap: "wrap", gap: 24 }}
      >
        {/* TODO: 부가 기능으로 들어온 경우 확대 화면에서 단어를 테두리로 강조 */}
        <div
          style={{
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            flex: "1 1 440px",
            height: 300,
            padding: 24,
            background: "#d9d9d9",
            fontSize: 17,
            fontWeight: 700,
            textAlign: "center",
          }}
        >
          썸네일 또는 페이지 스크린샷
        </div>
        <dl
          style={{
            display: "flex",
            flexDirection: "column",
            gap: 24,
            flex: "1 1 240px",
            paddingTop: 24,
          }}
        >
          {info.map((item) => (
            <div key={item.label}>
              <dt style={{ fontSize: 14, fontWeight: 700 }}>{item.label}</dt>
              <dd
                style={{
                  marginTop: 8,
                  padding: "6px 8px",
                  minHeight: 30,
                  border: "2px solid #ddd",
                  fontSize: 13,
                  wordBreak: "break-all",
                }}
              >
                {item.value}
              </dd>
            </div>
          ))}
        </dl>
      </section>

      <section style={{ marginTop: 48 }}>
        <header
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            marginBottom: 40,
          }}
        >
          <h2 style={{ fontSize: 20, fontWeight: 700 }}>
            의견 {OPINION_COUNT}개
          </h2>
          {/* TODO: 정렬 연결 */}
          <div style={{ display: "flex", gap: 20 }}>
            {["신뢰도순", "최신순"].map((sort) => (
              <button
                key={sort}
                type="button"
                style={{
                  border: "none",
                  background: "none",
                  fontSize: 14,
                  fontWeight: 700,
                  cursor: "pointer",
                }}
              >
                {sort}
              </button>
            ))}
          </div>
        </header>

        <div style={{ display: "flex", flexDirection: "column", gap: 70 }}>
          {OPINIONS.map((opinion) => (
            <OpinionCard
              key={opinion.id}
              opinion={opinion}
              detailHref={`/lookup/${slug}/${opinion.id}`}
            />
          ))}
        </div>
      </section>

      {/* TODO: 실제 현재 페이지와 전체 페이지 수 연결 */}
      <Pagination currentPage={1} totalPages={10} />
    </main>
  );
}
