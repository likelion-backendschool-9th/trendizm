import Link from "next/link";
import { notFound } from "next/navigation";
import { Suspense } from "react";
import Comments from "@/app/item/[id]/_components/Comments";
import Recommendations from "@/app/item/[id]/_components/Recommendations";
import RefreshAfterReissue from "@/app/item/_components/RefreshAfterReissue";
import ShareButton from "@/app/item/[id]/_components/ShareButton";
import { findCategory } from "@/app/item/_lib/categories";
import { accessTokenExpired, fetchAsViewer } from "@/app/item/_lib/fetchAsViewer";
import { answerLabel, formatDate } from "@/app/item/_lib/format";
import type {
  ItemAdjacentQuestions,
  ItemComment,
  ItemQuestion,
  ItemQuestionSummary,
  ItemRecommendation,
} from "@/app/item/_lib/types";
import { serverFetch } from "@/lib/api/server";
import { ApiError, type PageResponse } from "@/lib/api/types";

// 와이어프레임: 트렌드 아이템 - 상세페이지 (item/[id]) - 수정본
// 질문 글, 추천 아이템(잠금 해제·등록), 댓글(작성·답글·공감), 같은 소분류의 다른 질문을 보여 준다.
// 조회수, 공유(링크 복사), 이전/다음 글, 댓글 공감순 정렬을 함께 보여 준다.
// TODO(item): 나도 궁금해요·저장·신고·차단(ERD 추가 후), 질문자가 찾는 조건(질문 등록 때 선택), 해결 상태(기준 정한 뒤)

const COMMENT_PAGE_SIZE = 10;
const SIMILAR_SIZE = 5;

export default function ItemDetailPage({ params }: PageProps<"/item/[id]">) {
  return (
    <Suspense fallback={<p style={{ padding: 40 }}>불러오는 중...</p>}>
      <ItemDetail params={params} />
    </Suspense>
  );
}

async function ItemDetail({ params }: Pick<PageProps<"/item/[id]">, "params">) {
  const { id } = await params;
  if (!/^\d+$/.test(id)) {
    notFound();
  }

  let question: ItemQuestion;
  try {
    question = await fetchAsViewer<ItemQuestion>(`/api/items/${id}`);
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) {
      notFound();
    }
    throw error;
  }

  const [recommendations, comments, similar, adjacent, authExpired] = await Promise.all([
    fetchAsViewer<ItemRecommendation[]>(`/api/items/${id}/recommendations`),
    fetchAsViewer<PageResponse<ItemComment>>(
      `/api/items/${id}/comments?page=0&size=${COMMENT_PAGE_SIZE}`,
    ),
    serverFetch<PageResponse<ItemQuestionSummary>>(
      `/api/items?subcategory=${question.subcategory.slug}&size=${SIMILAR_SIZE + 1}`,
    ),
    serverFetch<ItemAdjacentQuestions>(`/api/items/${id}/adjacent`),
    accessTokenExpired(),
  ]);
  const similarQuestions = similar.content
    .filter((other) => other.id !== question.id)
    .slice(0, SIMILAR_SIZE);
  const category = findCategory(question.category.slug);

  return (
    <main
      style={{
        width: "100%",
        maxWidth: 1280,
        margin: "0 auto",
        padding: "40px 80px 80px",
        background: "#fff",
        color: "#111",
      }}
    >
      {/* 액세스 토큰만 만료돼 로그인하지 않은 사람 기준으로 그렸으면, 브라우저에서 재발급한 뒤 다시 그린다 */}
      {authExpired && <RefreshAfterReissue />}
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          gap: 16,
          marginBottom: 28,
        }}
      >
        <nav aria-label="경로" style={{ fontSize: 13, color: "#777" }}>
          <Link href="/item">‹ 트렌드 아이템</Link>
          <span style={{ margin: "0 8px" }}>/</span>
          <Link href={`/item/cate/${question.category.slug}`}>
            {question.category.label}
          </Link>
          <span style={{ margin: "0 8px" }}>/</span>
          <Link
            href={`/item/cate/${question.category.slug}/${question.subcategory.slug}`}
            style={{ fontWeight: 700, color: "#111" }}
          >
            {question.subcategory.label}
          </Link>
        </nav>
        <Link
          href="/item/new"
          style={{
            padding: "14px 20px",
            background: "#111",
            color: "#fff",
            fontSize: 14,
            fontWeight: 700,
          }}
        >
          + 질문하기
        </Link>
      </div>

      <div
        style={{
          display: "flex",
          flexWrap: "wrap",
          alignItems: "flex-start",
          gap: 30,
        }}
      >
        <div
          style={{
            display: "flex",
            flexDirection: "column",
            gap: 20,
            flex: "1 1 600px",
            minWidth: 0,
          }}
        >
          <article style={{ padding: "32px 36px", border: "1px solid #111" }}>
            <div style={{ display: "flex", gap: 8 }}>
              <span
                style={{
                  padding: "4px 10px",
                  border: "1px solid #111",
                  fontSize: 12,
                }}
              >
                {question.subcategory.label}
              </span>
              <span
                style={{
                  padding: "4px 10px",
                  border: "1px solid #111",
                  fontSize: 12,
                }}
              >
                {answerLabel(question.recommendationCount)}
              </span>
            </div>
            <h1 style={{ marginTop: 14, fontSize: 30, fontWeight: 700 }}>
              {question.title}
            </h1>
            <div
              style={{
                display: "flex",
                alignItems: "center",
                gap: 12,
                marginTop: 18,
                fontSize: 13,
                color: "#777",
              }}
            >
              <span
                aria-hidden
                style={{ width: 32, height: 32, background: "#e5e5e5" }}
              />
              <span style={{ fontWeight: 700, color: "#111" }}>
                {question.authorNickname}
                {question.mine && " (나)"}
              </span>
              · {formatDate(question.createdAt)} · 조회 {question.viewCount}
            </div>
            <p
              style={{
                marginTop: 18,
                fontSize: 16,
                lineHeight: 1.7,
                whiteSpace: "pre-wrap",
              }}
            >
              {question.content}
            </p>
            <div
              style={{
                display: "flex",
                gap: 8,
                marginTop: 18,
                paddingTop: 16,
                borderTop: "1px solid #ddd",
              }}
            >
              <a
                href="#recommendations"
                style={{
                  padding: "8px 14px",
                  border: "1px solid #111",
                  fontSize: 13,
                }}
              >
                🛍️ 추천 아이템 {question.recommendationCount}
              </a>
              <a
                href="#comments"
                style={{
                  padding: "8px 14px",
                  border: "1px solid #111",
                  fontSize: 13,
                }}
              >
                💬 댓글 {question.commentCount}
              </a>
              <span style={{ marginLeft: "auto" }}>
                <ShareButton title={question.title} />
              </span>
            </div>
          </article>

          <Recommendations
            questionId={question.id}
            recommendations={recommendations}
          />

          <Comments
            // 댓글 목록은 처음 받은 값을 상태에 담아 두므로, 토큰 재발급 뒤 다시 그려도 공감 여부가 그대로 남는다.
            // 재발급 뒤 authExpired가 바뀌면 key가 바뀌어 새 목록으로 다시 시작한다.
            key={`${question.id}-${authExpired ? "expired" : "viewer"}`}
            questionId={question.id}
            commentCount={question.commentCount}
            initialPage={comments}
            pageSize={COMMENT_PAGE_SIZE}
          />

          <nav aria-label="글 이동">
            <ul style={{ listStyle: "none", border: "1px solid #111" }}>
              {[
                { label: "이전 글", link: adjacent.previous },
                { label: "다음 글", link: adjacent.next },
              ].map(({ label, link }, index) => (
                <li
                  key={label}
                  style={{
                    display: "flex",
                    gap: 32,
                    padding: "18px 24px",
                    borderTop: index === 0 ? "none" : "1px solid #ddd",
                    fontSize: 14,
                  }}
                >
                  <span style={{ flexShrink: 0, color: "#777" }}>{label}</span>
                  {link ? (
                    <Link href={`/item/${link.id}`}>{link.title}</Link>
                  ) : (
                    <span style={{ color: "#aaa" }}>
                      {label === "이전 글" ? "첫 번째 글이에요" : "마지막 글이에요"}
                    </span>
                  )}
                </li>
              ))}
            </ul>
          </nav>

          <Link
            href={`/item/cate/${question.category.slug}/${question.subcategory.slug}`}
            style={{
              display: "block",
              width: 144,
              margin: "8px auto 0",
              padding: "12px 0",
              border: "1px solid #111",
              textAlign: "center",
              fontSize: 14,
            }}
          >
            ☰ 목록으로
          </Link>
        </div>

        <aside
          style={{
            display: "flex",
            flexDirection: "column",
            gap: 20,
            flex: "0 1 356px",
          }}
        >
          <section style={{ padding: 24, border: "1px solid #111" }}>
            <h2 style={{ fontSize: 17, fontWeight: 700 }}>
              {question.subcategory.label}의 다른 질문
            </h2>
            {similarQuestions.length === 0 ? (
              <p style={{ marginTop: 12, fontSize: 13, color: "#777" }}>
                아직 다른 질문이 없어요.
              </p>
            ) : (
              <ul style={{ marginTop: 8, listStyle: "none" }}>
                {similarQuestions.map((other, index) => (
                  <li
                    key={other.id}
                    style={{
                      padding: "14px 0",
                      borderTop: index === 0 ? "none" : "1px solid #ddd",
                    }}
                  >
                    <Link href={`/item/${other.id}`} style={{ fontSize: 14 }}>
                      {other.title}
                    </Link>
                    <p style={{ marginTop: 6, fontSize: 12, color: "#777" }}>
                      {answerLabel(other.recommendationCount)} · 댓글{" "}
                      {other.commentCount}
                    </p>
                  </li>
                ))}
              </ul>
            )}
          </section>

          {category && (
            <section style={{ padding: 24, border: "1px solid #111" }}>
              <h2 style={{ fontSize: 17, fontWeight: 700 }}>
                {category.name} 카테고리
              </h2>
              <ul
                style={{
                  display: "flex",
                  flexWrap: "wrap",
                  gap: 8,
                  marginTop: 16,
                  listStyle: "none",
                }}
              >
                {category.subcategories.map((subcategory) => {
                  const selected = subcategory.slug === question.subcategory.slug;

                  return (
                    <li key={subcategory.slug}>
                      <Link
                        href={`/item/cate/${category.slug}/${subcategory.slug}`}
                        aria-current={selected ? "page" : undefined}
                        style={{
                          display: "block",
                          padding: "7px 12px",
                          border: "1px solid #111",
                          background: selected ? "#111" : "#fff",
                          color: selected ? "#fff" : "#111",
                          fontSize: 12,
                          fontWeight: selected ? 700 : 400,
                        }}
                      >
                        {subcategory.name}
                      </Link>
                    </li>
                  );
                })}
              </ul>
            </section>
          )}
        </aside>
      </div>
    </main>
  );
}
