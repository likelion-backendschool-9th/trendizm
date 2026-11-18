import Link from "next/link";

export type RankingFilter = {
  slug: string;
  name: string;
};

export type Ranking = {
  rank: number;
  keyword: string;
  summary: string;
  links: { id: number; title: string; date: string; description: string }[];
};

type RankingListProps = {
  platforms: RankingFilter[];
  categories: RankingFilter[];
  platform?: string;
  category?: string;
  rankings: Ranking[];
};

// 와이어프레임: 플랫폼별 트렌드 순위
// /ranking, /ranking/[platform], /ranking/[platform]/[category]가 함께 쓴다.
export default function RankingList({
  platforms,
  categories,
  platform,
  category,
  rankings,
}: RankingListProps) {
  const categoryPlatform = platform ?? platforms[0].slug;

  return (
    <main
      style={{
        width: "100%",
        maxWidth: 800,
        margin: "0 auto",
        padding: "48px 30px 64px",
        background: "#fff",
        color: "#111",
      }}
    >
      {/* 와이어프레임에는 제목이 보이지 않아 화면에서는 숨기고 스크린리더에만 읽힌다. */}
      <h1
        style={{
          position: "absolute",
          width: 1,
          height: 1,
          overflow: "hidden",
          clip: "rect(0 0 0 0)",
          whiteSpace: "nowrap",
        }}
      >
        플랫폼별 트렌드 순위
      </h1>

      <nav aria-label="플랫폼">
        <ul style={{ display: "flex", flexWrap: "wrap", gap: 10, listStyle: "none" }}>
          {platforms.map((item) => {
            const selected = item.slug === platform;

            return (
              <li key={item.slug}>
                <Link
                  href={`/ranking/${item.slug}`}
                  aria-current={selected ? "page" : undefined}
                  style={{
                    display: "block",
                    width: 118,
                    padding: "6px 0",
                    border: "1px solid #111",
                    background: selected ? "#111" : "#fff",
                    color: selected ? "#fff" : "#111",
                    textAlign: "center",
                    fontSize: 18,
                  }}
                >
                  {item.name}
                </Link>
              </li>
            );
          })}
        </ul>
      </nav>

      <nav aria-label="카테고리" style={{ marginTop: 10 }}>
        <ul style={{ display: "flex", flexWrap: "wrap", gap: 6, listStyle: "none" }}>
          {categories.map((item) => {
            const selected = item.slug === category;

            return (
              <li key={item.slug}>
                <Link
                  href={`/ranking/${categoryPlatform}/${item.slug}`}
                  aria-current={selected ? "page" : undefined}
                  style={{
                    display: "block",
                    // 'AI'처럼 짧은 영문 항목도 한글 항목과 같은 크기로 보이게 한다.
                    // 줄 높이를 고정해 글꼴 차이로 높이가 달라지지 않게 하고,
                    // 최소 너비는 가장 짧은 한글 항목(반려동물) 정도로 둔다.
                    minWidth: 64,
                    padding: "6px 12px",
                    lineHeight: "16px",
                    textAlign: "center",
                    border: "1px solid #999",
                    borderRadius: 999,
                    background: selected ? "#111" : "#fff",
                    color: selected ? "#fff" : "#111",
                    fontSize: 14,
                    fontWeight: 700,
                  }}
                >
                  {item.name}
                </Link>
              </li>
            );
          })}
        </ul>
      </nav>

      <section style={{ marginTop: 24 }}>
        <h2
          style={{
            position: "absolute",
            width: 1,
            height: 1,
            overflow: "hidden",
            clip: "rect(0 0 0 0)",
            whiteSpace: "nowrap",
          }}
        >
          순위
        </h2>
        <ol
          style={{
            display: "grid",
            gridTemplateColumns: "repeat(2, minmax(0, 1fr))",
            gap: "8px 24px",
            padding: "0 10px",
            listStyle: "none",
          }}
        >
          {rankings.map((ranking) => (
            <li
              key={ranking.rank}
              style={{ display: "flex", alignItems: "baseline", gap: 16, fontSize: 22 }}
            >
              <span style={{ width: 32, fontSize: 28, fontWeight: 700 }}>
                {ranking.rank}
              </span>
              {ranking.keyword}
            </li>
          ))}
        </ol>
      </section>

      <section
        style={{ display: "flex", flexDirection: "column", gap: 20, marginTop: 40 }}
      >
        <h2
          style={{
            position: "absolute",
            width: 1,
            height: 1,
            overflow: "hidden",
            clip: "rect(0 0 0 0)",
            whiteSpace: "nowrap",
          }}
        >
          순위별 상세
        </h2>
        {/* TODO: 로테이션 형태로 하나가 접히고 그 다음 하나가 펼쳐짐 */}
        {/* TODO: 차트 안에 채팅 들어감 */}
        {rankings.map((ranking, index) => {
          // 처음 펼쳐진 카드는 요약 전체를, 접힌 카드는 한 줄만 보여 준다.
          const open = index === 0;

          return (
            <details
              key={ranking.rank}
              open={open}
              style={{ padding: "24px 24px 28px", border: "1px solid #111" }}
            >
              <summary style={{ listStyle: "none", cursor: "pointer" }}>
                <span
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                  }}
                >
                  <span style={{ display: "flex", alignItems: "baseline", gap: 24 }}>
                    <span style={{ fontSize: 40, fontWeight: 700 }}>{ranking.rank}</span>
                    <span style={{ fontSize: 36 }}>{ranking.keyword}</span>
                  </span>
                  <span aria-hidden style={{ fontSize: 18, color: "#ccc" }}>
                    {open ? "▲" : "▼"}
                  </span>
                </span>
                <span
                  style={{
                    display: "flex",
                    flexDirection: open ? "column" : "row",
                    alignItems: open ? "flex-start" : "center",
                    gap: open ? 14 : 10,
                    marginTop: 10,
                  }}
                >
                  <span
                    style={{
                      flexShrink: 0,
                      padding: "2px 10px",
                      border: "1px solid #111",
                      fontSize: open ? 16 : 14,
                    }}
                  >
                    AI 요약
                  </span>
                  <span
                    style={
                      open
                        ? { fontSize: 18, lineHeight: 1.5 }
                        : {
                            minWidth: 0,
                            overflow: "hidden",
                            textOverflow: "ellipsis",
                            whiteSpace: "nowrap",
                            fontSize: 13,
                          }
                    }
                  >
                    {ranking.summary}
                  </span>
                </span>
              </summary>

              <h3
                style={{
                  display: "inline-block",
                  marginTop: 18,
                  padding: "2px 16px",
                  border: "1px solid #111",
                  fontSize: 16,
                  fontWeight: 400,
                }}
              >
                관련 링크
              </h3>
              {/* 관련 링크 썸네일은 그 플랫폼의 정책상 빠져야 하는 경우 뺀다. */}
              <div style={{ display: "flex", alignItems: "flex-start", gap: 20, marginTop: 16 }}>
                <ul
                  style={{
                    display: "grid",
                    gridTemplateColumns: "repeat(3, minmax(0, 1fr))",
                    gap: 20,
                    flex: 1,
                    listStyle: "none",
                  }}
                >
                  {ranking.links.map((link) => (
                    <li key={link.id}>
                      <div aria-hidden style={{ height: 100, background: "#d9d9d9" }} />
                      <p
                        style={{
                          display: "flex",
                          justifyContent: "space-between",
                          alignItems: "baseline",
                          marginTop: 10,
                        }}
                      >
                        <span style={{ fontSize: 14, fontWeight: 700 }}>{link.title}</span>
                        <time style={{ fontSize: 11 }}>{link.date}</time>
                      </p>
                      <p style={{ marginTop: 6, fontSize: 11, color: "#555" }}>
                        {link.description}
                      </p>
                    </li>
                  ))}
                </ul>
                <button
                  type="button"
                  style={{
                    display: "flex",
                    flexDirection: "column",
                    alignItems: "center",
                    gap: 8,
                    marginTop: 30,
                    border: "none",
                    background: "none",
                    fontSize: 15,
                    cursor: "pointer",
                  }}
                >
                  <span
                    aria-hidden
                    style={{
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      width: 40,
                      height: 40,
                      border: "1px solid #999",
                      borderRadius: "50%",
                      background: "#f5f5f5",
                      fontSize: 18,
                    }}
                  >
                    ›
                  </span>
                  더보기
                </button>
              </div>
            </details>
          );
        })}
      </section>
    </main>
  );
}
