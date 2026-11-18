import Link from "next/link";
import Pagination from "@/components/Pagination";

// 와이어프레임: 밈 검색 - 리스트

const DOCUMENTS = [
  {
    id: 1,
    word: "알잘딱깔센",
    opinionCount: 10,
    siteUrl: "video.example.com/watch/01",
    topOpinion: {
      id: 1,
      title: "의견 제목",
      author: "게시자",
      createdAt: "2026-01-01 14:00",
      agreeCount: 24,
    },
    quotes: ["“이 자료는 알잘딱깔센으로 정리해 줘.”", "“설명 안 해도 알잘딱깔센이네.”"],
    comments: [
      {
        id: 1,
        author: "사용자 01",
        createdAt: "2026-01-01 15:00",
        body: "사용 예시를 보니 어떤 상황에서 쓰는지 알겠어요.",
      },
      {
        id: 2,
        author: "사용자 02",
        createdAt: "2026-01-01 15:10",
        body: "영상에서는 일을 깔끔하게 끝내 달라는 의미 같네요.",
      },
    ],
  },
  {
    id: 2,
    word: "중꺾마",
    opinionCount: 7,
    siteUrl: "blog.example.com/meme/03",
    topOpinion: {
      id: 3,
      title: "의견 제목",
      author: "게시자",
      createdAt: "2026-01-02 10:00",
      agreeCount: 11,
    },
    quotes: ["“결과는 아쉽지만 중꺾마로 다시 도전하자.”", "“끝까지 해보는 거야. 중꺾마!”"],
    comments: [
      {
        id: 3,
        author: "사용자 05",
        createdAt: "2026-01-02 11:00",
        body: "결과보다 계속 도전하는 마음이 중요하다는 말이네요.",
      },
    ],
  },
];

export default function LookupRecentsPage() {
  return (
    <main
      style={{
        width: "100%",
        maxWidth: 800,
        margin: "0 auto",
        padding: "28px 40px 48px",
        background: "#fff",
        color: "#222",
      }}
    >
      <header>
        <h1 style={{ fontSize: 26, fontWeight: 700 }}>최근 등록된 문서</h1>
        <p style={{ marginTop: 14, fontSize: 13, lineHeight: 1.6, color: "#555" }}>
          모든 사용자가 검색한 단어와 사이트, 함께 나눈 의견을 모았어요.
          <br />
          (의견 1개 이상인 문서만 등록됩니다.)
        </p>
        {/* TODO: 사이트 필터 연결 */}
        <select
          name="site"
          defaultValue="all"
          style={{
            width: 128,
            height: 36,
            marginTop: 16,
            padding: "0 12px",
            border: "1px solid #ccc",
            borderRadius: 6,
            background: "#fff",
            fontSize: 13,
          }}
        >
          <option value="all">전체 사이트</option>
        </select>
      </header>

      <section style={{ marginTop: 24 }}>
        <header
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            marginBottom: 24,
          }}
        >
          <h2 style={{ fontSize: 15, fontWeight: 700 }}>
            최근 등록 문서 {DOCUMENTS.length}개
          </h2>
          {/* TODO: 정렬 연결 */}
          <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
            {["최근 등록순", "의견 많은순"].map((sort, index) => (
              <button
                key={sort}
                type="button"
                style={{
                  paddingLeft: index === 0 ? 0 : 8,
                  border: "none",
                  borderLeft: index === 0 ? "none" : "1px solid #ccc",
                  background: "none",
                  color: "#555",
                  fontSize: 12,
                  cursor: "pointer",
                }}
              >
                {sort}
              </button>
            ))}
          </div>
        </header>

        <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
          {DOCUMENTS.map((document) => (
            <article
              key={document.id}
              style={{
                padding: "20px 20px 24px",
                border: "1px solid #ccc",
                borderRadius: 8,
              }}
            >
              <header>
                <div
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "flex-start",
                  }}
                >
                  <h3 style={{ fontSize: 22, fontWeight: 700 }}>
                    <Link href={`/lookup/${document.word}`}>{document.word}</Link>
                  </h3>
                  <p
                    style={{
                      padding: "6px 12px",
                      borderRadius: 4,
                      background: "#f0f0ef",
                      fontSize: 12,
                      fontWeight: 700,
                    }}
                  >
                    의견 {document.opinionCount}개
                  </p>
                </div>
                <p style={{ marginTop: 12, fontSize: 12, color: "#666" }}>
                  {document.siteUrl}
                </p>
              </header>

              <section aria-label="가장 많이 동의한 의견" style={{ marginTop: 12 }}>
                <p
                  style={{
                    display: "inline-block",
                    padding: "6px 10px",
                    border: "1px solid #ccc",
                    borderRadius: 4,
                    background: "#f0f0ef",
                    fontSize: 12,
                    fontWeight: 700,
                  }}
                >
                  가장 많이 동의한 의견
                </p>
                <p
                  style={{
                    display: "flex",
                    alignItems: "baseline",
                    flexWrap: "wrap",
                    gap: 10,
                    marginTop: 10,
                  }}
                >
                  <span style={{ fontSize: 19, fontWeight: 700 }}>
                    {document.topOpinion.title}
                  </span>
                  <span style={{ fontSize: 12, color: "#777" }}>
                    {document.topOpinion.author} ·{" "}
                    <time>{document.topOpinion.createdAt}</time>
                  </span>
                </p>
                <p style={{ marginTop: 12, fontSize: 12, fontWeight: 700, color: "#555" }}>
                  {document.topOpinion.agreeCount}명이 동의
                </p>
                <ul
                  aria-label="작성자가 남긴 인용 예문"
                  style={{
                    display: "flex",
                    flexDirection: "column",
                    gap: 10,
                    marginTop: 14,
                    padding: "14px 20px",
                    borderLeft: "3px solid #bbb",
                    background: "#f3f3f2",
                    listStyle: "none",
                    fontSize: 14,
                  }}
                >
                  {document.quotes.map((quote) => (
                    <li key={quote}>{quote}</li>
                  ))}
                </ul>
              </section>

              {/* 대댓글은 표시하지 않는다. */}
              <div style={{ position: "relative", marginTop: 12 }}>
                <details>
                  <summary
                    style={{
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      width: 158,
                      height: 36,
                      border: "1px solid #ccc",
                      borderRadius: 6,
                      listStyle: "none",
                      fontSize: 12,
                      fontWeight: 700,
                      cursor: "pointer",
                    }}
                  >
                    댓글 {document.comments.length}개
                  </summary>
                  <div
                    style={{
                      marginTop: 12,
                      padding: "12px 12px 16px",
                      background: "#f3f3f2",
                    }}
                  >
                    <p
                      style={{
                        display: "flex",
                        alignItems: "center",
                        gap: 8,
                        paddingBottom: 12,
                        borderBottom: "1px solid #ccc",
                        fontSize: 13,
                        fontWeight: 700,
                      }}
                    >
                      이 의견에 달린 댓글
                      <span
                        style={{
                          display: "inline-flex",
                          alignItems: "center",
                          justifyContent: "center",
                          width: 26,
                          height: 26,
                          border: "1px solid #ccc",
                          borderRadius: "50%",
                          background: "#fff",
                          fontSize: 12,
                        }}
                      >
                        {document.comments.length}
                      </span>
                    </p>
                    <ul
                      style={{
                        display: "flex",
                        flexDirection: "column",
                        gap: 14,
                        marginTop: 14,
                        listStyle: "none",
                      }}
                    >
                      {document.comments.map((comment) => (
                        <li key={comment.id}>
                          <p style={{ fontSize: 11, color: "#666" }}>
                            <span style={{ fontWeight: 700 }}>{comment.author}</span>
                            {"  "}
                            <time>{comment.createdAt}</time>
                          </p>
                          <p style={{ marginTop: 6, fontSize: 12 }}>{comment.body}</p>
                        </li>
                      ))}
                    </ul>
                  </div>
                </details>
                {/* 접기 버튼 옆에 놓으려고 위치를 고정한다. */}
                <Link
                  href={`/lookup/${document.word}/${document.topOpinion.id}`}
                  style={{
                    position: "absolute",
                    top: 0,
                    left: 158,
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    width: 158,
                    height: 36,
                    fontSize: 12,
                    fontWeight: 700,
                  }}
                >
                  의견 전체 보기
                </Link>
              </div>
            </article>
          ))}
        </div>
      </section>

      <p style={{ marginTop: 24, fontSize: 11, color: "#666" }}>
        1–{DOCUMENTS.length} / {DOCUMENTS.length}개 항목
      </p>
      {/* TODO: 실제 현재 페이지와 전체 페이지 수 연결 */}
      <Pagination currentPage={1} totalPages={1} />
    </main>
  );
}
