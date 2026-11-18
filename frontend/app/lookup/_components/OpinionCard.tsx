import Link from "next/link";
import type { ReactNode } from "react";

export type OpinionReply = {
  id: number;
  author: string;
  body: string;
  createdAt: string;
  likeCount: number;
};

export type OpinionComment = OpinionReply & {
  replies: OpinionReply[];
};

export type Opinion = {
  id: number;
  meaning: string;
  author: string;
  agreeCount: number;
  description: string;
  examples: string[];
  comments: OpinionComment[];
};

type OpinionCardProps = {
  opinion: Opinion;
  // 주면 뜻 제목이 의견 상세로 가는 링크가 된다.
  detailHref?: string;
};

// 밈 검색 - 결과의 의견 카드. 결과 화면과 의견 상세 화면에서 함께 쓴다.
export default function OpinionCard({ opinion, detailHref }: OpinionCardProps) {
  return (
    <article style={{ padding: "28px 28px 32px", border: "1px solid #111" }}>
      <header
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "flex-end",
          gap: 16,
        }}
      >
        <h3
          style={{
            display: "flex",
            alignItems: "baseline",
            flexWrap: "wrap",
            gap: 10,
            fontSize: 30,
            fontWeight: 700,
          }}
        >
          {detailHref ? (
            <Link href={detailHref}>{opinion.meaning}</Link>
          ) : (
            opinion.meaning
          )}
          <span style={{ fontSize: 14, fontWeight: 400, color: "#555" }}>
            동의자 {opinion.agreeCount}명
          </span>
        </h3>
        <p style={{ flexShrink: 0, fontSize: 15 }}>{opinion.author}</p>
      </header>

      <p style={{ marginTop: 18, fontSize: 19, lineHeight: 1.5 }}>
        {opinion.description}
      </p>

      <ul
        aria-label="사용 예시"
        style={{
          display: "flex",
          flexDirection: "column",
          gap: 6,
          marginTop: 16,
          padding: "4px 0 4px 44px",
          borderLeft: "5px solid #ddd",
          listStyle: "square",
          fontSize: 19,
        }}
      >
        {opinion.examples.map((example) => (
          <li key={example}>{example}</li>
        ))}
      </ul>

      {/* TODO: 동의/비동의 요청 연결 */}
      <div style={{ display: "flex", gap: 10, marginTop: 20 }}>
        {["✅ 동의", "☑️ 비동의"].map((label) => (
          <button
            key={label}
            type="button"
            style={{
              width: 100,
              height: 26,
              border: "1px solid #111",
              background: "#fff",
              fontSize: 13,
              cursor: "pointer",
            }}
          >
            {label}
          </button>
        ))}
      </div>

      <section style={{ marginTop: 36 }}>
        <h4 style={{ fontSize: 20, fontWeight: 700 }}>댓글</h4>

        {/* TODO: 댓글 작성 요청 연결 */}
        <form
          style={{
            display: "flex",
            flexDirection: "column",
            alignItems: "flex-end",
            gap: 10,
            marginTop: 16,
          }}
        >
          <textarea
            name="comment"
            placeholder="댓글을 입력하세요"
            style={{
              width: "100%",
              height: 80,
              padding: 12,
              border: "1px solid #ddd",
              resize: "vertical",
              fontSize: 14,
            }}
          />
          <button
            type="submit"
            style={{
              width: 40,
              height: 22,
              border: "1px solid #999",
              background: "#fff",
              fontSize: 11,
              cursor: "pointer",
            }}
          >
            전송
          </button>
        </form>

        {opinion.comments.length > 0 && (
          <ul style={{ marginTop: 12, listStyle: "none" }}>
            {opinion.comments.map((comment) => (
              <li key={comment.id} style={{ padding: "8px 0" }}>
                <CommentBody comment={comment}>
                  <span>💬 {comment.replies.length}</span>
                  {/* TODO: 대댓글 입력창 열기 */}
                  <button
                    type="button"
                    style={{
                      padding: "1px 8px",
                      border: "1px solid #999",
                      background: "#fff",
                      fontSize: 9,
                      cursor: "pointer",
                    }}
                  >
                    대댓글 작성
                  </button>
                </CommentBody>

                {comment.replies.length > 0 && (
                  <ul
                    style={{
                      display: "flex",
                      flexDirection: "column",
                      gap: 24,
                      marginTop: 16,
                      padding: "16px 10px",
                      listStyle: "none",
                      background: "#f9f9f9",
                    }}
                  >
                    {comment.replies.map((reply) => (
                      <li key={reply.id}>
                        <CommentBody comment={reply} />
                      </li>
                    ))}
                  </ul>
                )}
              </li>
            ))}
          </ul>
        )}
      </section>
    </article>
  );
}

function CommentBody({
  comment,
  children,
}: {
  comment: OpinionReply;
  // 메타 줄 끝에 붙일 내용 (답글 수, 대댓글 버튼 등)
  children?: ReactNode;
}) {
  return (
    <>
      <p style={{ fontSize: 11, color: "#444" }}>{comment.author}</p>
      <p style={{ marginTop: 8, fontSize: 14 }}>{comment.body}</p>
      <p
        style={{
          display: "flex",
          alignItems: "center",
          gap: 8,
          marginTop: 8,
          fontSize: 9,
          color: "#555",
        }}
      >
        <span>
          🕒 <time>{comment.createdAt}</time>
        </span>
        <span>❤️ {comment.likeCount}</span>
        {children}
      </p>
    </>
  );
}
