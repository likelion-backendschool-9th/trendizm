import Link from "next/link";
import CategoryNav from "@/app/item/_components/CategoryNav";
import { answerLabel, formatDate } from "@/app/item/_lib/format";
import type { ItemQuestionSummary } from "@/app/item/_lib/types";
import Pagination from "@/components/Pagination";
import { toUiPage, type PageResponse } from "@/lib/api/types";

type ItemListProps = {
  cate?: string;
  subcate?: string;
  questions: PageResponse<ItemQuestionSummary>;
};

// 와이어프레임: 트렌드 아이템 (item) - 수정본
// /item, /item/cate/[cate], /item/cate/[cate]/[subcate]가 함께 쓴다.
// 목록 API에는 추천 아이템 내용이 없어서 오른쪽 칸에는 추천 수만 보여 주고, 아이템은 상세에서 연다.
// TODO(item): 추천 버튼 + 캐러셀(중간 발표 뒤)에서 목록 응답에 추천 아이템이 오면 미리보기를 붙인다.
export default function ItemList({ cate, subcate, questions }: ItemListProps) {
  return (
    <main
      style={{
        width: "100%",
        maxWidth: 800,
        margin: "0 auto",
        padding: "24px 32px 48px",
        background: "#fff",
        color: "#111",
      }}
    >
      <header
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "flex-start",
          gap: 16,
          marginBottom: 24,
        }}
      >
        <div>
          <h1 style={{ fontSize: 26, fontWeight: 700 }}>트렌드 아이템</h1>
          <p style={{ marginTop: 6, fontSize: 13, color: "#777" }}>
            원하는 아이템을 물어보고, 딱 맞는 추천을 받아보세요
          </p>
        </div>
        <Link
          href="/item/new"
          style={{
            padding: "8px 24px",
            border: "1px solid #999",
            background: "#fff",
            fontSize: 13,
          }}
        >
          + 질문하기
        </Link>
      </header>

      <CategoryNav cate={cate} subcate={subcate} />

      <p style={{ margin: "18px 0 16px", fontSize: 13 }}>
        질문 {questions.totalElements}개 · 최신순
      </p>

      {questions.content.length === 0 ? (
        <p
          style={{
            padding: "48px 0",
            border: "1px solid #ddd",
            textAlign: "center",
            fontSize: 14,
            color: "#777",
          }}
        >
          아직 질문이 없어요. 첫 질문을 올려 보세요.
        </p>
      ) : (
        <section
          aria-label="질문 목록"
          style={{ display: "flex", flexDirection: "column", gap: 16 }}
        >
          {questions.content.map((question) => (
            <article
              key={question.id}
              style={{
                display: "flex",
                gap: 24,
                padding: 16,
                border: "1px solid #999",
              }}
            >
              <div
                style={{
                  display: "flex",
                  flexDirection: "column",
                  flex: 1,
                  minWidth: 0,
                }}
              >
                <div style={{ display: "flex", gap: 6 }}>
                  <span
                    style={{
                      padding: "4px 10px",
                      border: "1px solid #999",
                      fontSize: 12,
                    }}
                  >
                    {question.subcategory.label}
                  </span>
                  <span
                    style={{
                      padding: "4px 10px",
                      border: "1px solid #999",
                      background: "#eee",
                      fontSize: 12,
                    }}
                  >
                    {answerLabel(question.recommendationCount)}
                  </span>
                </div>
                <h2 style={{ marginTop: 16, fontSize: 20, fontWeight: 700 }}>
                  <Link href={`/item/${question.id}`}>{question.title}</Link>
                </h2>
                <p
                  style={{
                    marginTop: 8,
                    fontSize: 13,
                    lineHeight: 1.6,
                    // 두 줄이 넘으면 말줄임표로 자른다.
                    display: "-webkit-box",
                    WebkitLineClamp: 2,
                    WebkitBoxOrient: "vertical",
                    overflow: "hidden",
                  }}
                >
                  {question.contentPreview}
                </p>
                <p
                  style={{
                    marginTop: "auto",
                    paddingTop: 24,
                    fontSize: 11,
                    color: "#777",
                  }}
                >
                  {question.authorNickname} | {formatDate(question.createdAt)} |
                  댓글 {question.commentCount}
                </p>
              </div>

              <Link
                href={`/item/${question.id}#recommendations`}
                aria-label={`${question.title} 추천 아이템 보기`}
                style={{
                  display: "flex",
                  flexDirection: "column",
                  alignItems: "center",
                  justifyContent: "center",
                  gap: 10,
                  flexShrink: 0,
                  width: 184,
                  height: 106,
                  border: "1px solid #999",
                  background: "#eee",
                  fontSize: 12,
                  fontWeight: 700,
                  textAlign: "center",
                }}
              >
                <span aria-hidden style={{ fontSize: 22 }}>
                  {question.recommendationCount > 0 ? "🔒" : "💬"}
                </span>
                {question.recommendationCount > 0
                  ? `추천 아이템 ${question.recommendationCount}개 보기`
                  : "아는 아이템 추천하기"}
              </Link>
            </article>
          ))}
        </section>
      )}

      <Pagination
        currentPage={toUiPage(questions.page)}
        totalPages={questions.totalPages}
      />
    </main>
  );
}
