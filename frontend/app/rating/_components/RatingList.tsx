import Link from "next/link";
import RatingCategoryNav from "@/app/rating/_components/RatingCategoryNav";
import RatingImage from "@/app/rating/_components/RatingImage";
import Stars from "@/app/rating/_components/Stars";
import {
  DEFAULT_STATUS_LABELS,
  RATING_STATUSES,
  findCategory,
  type RatingStatusSlug,
} from "@/app/rating/_lib/categories";
import { formatDate, formatScore } from "@/app/rating/_lib/format";
import type { RatingContentSummary, SentimentStats } from "@/app/rating/_lib/types";
import Pagination from "@/components/Pagination";
import { toUiPage, type PageResponse } from "@/lib/api/types";

type RatingListProps = {
  cate?: string;
  subcate?: string;
  status?: RatingStatusSlug;
  contents: PageResponse<RatingContentSummary>;
};

// 관람 등급 배지 색
const AGE_COLORS: Record<number, string> = {
  19: "#a33",
  15: "#8a5a00",
  12: "#2f7a3a",
};

// 와이어프레임: 컨텐츠 별 호/불호 평점 (rating) - 수정본
// /rating, /rating/cate/[cate], /rating/cate/[cate]/[subcate]가 함께 쓴다.
// 목록 API에는 후기가 없어서 카드에는 집계(비율·평균 별점)만 보여 주고, 후기는 상세에서 본다.
// TODO(rating): 사용자군 태그(TOP5)는 회원 프로필 조회(findProfiles)가 생기면 붙인다.
export default function RatingList({ cate, subcate, status, contents }: RatingListProps) {
  const category = cate ? findCategory(cate) : undefined;
  const statusLabels = category?.statusLabels ?? DEFAULT_STATUS_LABELS;
  const basePath = ["/rating", cate && `cate/${cate}`, subcate].filter(Boolean).join("/");
  const currentSub = category?.subcategories.find((sub) => sub.slug === subcate)?.name;

  return (
    <main
      style={{
        width: "100%",
        maxWidth: 800,
        margin: "0 auto",
        padding: "32px 48px 64px",
        background: "#fff",
        color: "#111",
      }}
    >
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
        컨텐츠 별 호/불호 평점
      </h1>

      <RatingCategoryNav cate={cate} subcate={subcate} />

      <div
        style={{
          display: "flex",
          flexWrap: "wrap",
          alignItems: "center",
          gap: 12,
          margin: "24px 0 20px",
        }}
      >
        <p style={{ fontSize: 13 }}>
          {category ? category.name : "전체"}
          {currentSub && ` > ${currentSub}`}
          <strong style={{ marginLeft: 8 }}>{contents.totalElements}개</strong>
          <span style={{ marginLeft: 8, color: "#777" }}>· 최신 등록순</span>
        </p>
        <ul
          aria-label="진행 상태"
          style={{ display: "flex", gap: 6, marginLeft: "auto", listStyle: "none" }}
        >
          {[undefined, ...RATING_STATUSES].map((option) => {
            const selected = option === status;
            return (
              <li key={option ?? "all"}>
                <Link
                  href={option ? `${basePath}?status=${option}` : basePath}
                  aria-current={selected ? "page" : undefined}
                  style={{
                    display: "block",
                    padding: "5px 10px",
                    border: "1px solid #222",
                    borderRadius: 6,
                    background: selected ? "#111" : "#fff",
                    color: selected ? "#fff" : "#111",
                    fontSize: 12,
                  }}
                >
                  {option ? statusLabels[option] : "전체"}
                </Link>
              </li>
            );
          })}
        </ul>
      </div>

      {contents.content.length === 0 ? (
        <p
          style={{
            padding: "48px 0",
            border: "1px solid #ddd",
            borderRadius: 12,
            textAlign: "center",
            fontSize: 14,
            color: "#777",
          }}
        >
          아직 등록된 콘텐츠가 없어요.
        </p>
      ) : (
        <section
          aria-label="콘텐츠 목록"
          style={{ display: "flex", flexDirection: "column", gap: 24 }}
        >
          {contents.content.map((content) => (
            <article
              key={content.id}
              style={{ padding: 24, border: "1px solid #222", borderRadius: 12 }}
            >
              <header style={{ display: "flex", gap: 24 }}>
                <RatingImage src={content.imageUrl} alt={content.title} width={112} height={160} />
                <div style={{ flex: 1, minWidth: 0 }}>
                  <p style={{ fontSize: 12 }}>
                    {content.category.label} &gt; {content.subcategory.label}
                  </p>
                  <h2
                    style={{
                      display: "flex",
                      alignItems: "center",
                      gap: 14,
                      marginTop: 8,
                      fontSize: 24,
                      fontWeight: 700,
                    }}
                  >
                    <Link href={`/rating/${content.id}/like`}>{content.title}</Link>
                    {content.ageRating !== null && <AgeBadge age={content.ageRating} />}
                  </h2>
                  <p
                    style={{
                      display: "flex",
                      flexWrap: "wrap",
                      alignItems: "center",
                      gap: 14,
                      marginTop: 10,
                      fontSize: 12,
                      color: "#444",
                    }}
                  >
                    <span
                      style={{
                        padding: "2px 6px",
                        border: "1px solid #222",
                        borderRadius: 3,
                        color: "#111",
                        fontWeight: 700,
                      }}
                    >
                      {content.status.label}
                    </span>
                    {content.releasedAt && <span>{formatDate(content.releasedAt)} 공개</span>}
                  </p>
                  <RatioBar stats={content.stats} />
                </div>
              </header>

              <div
                style={{
                  display: "grid",
                  gridTemplateColumns: "repeat(auto-fit, minmax(260px, 1fr))",
                  gap: 16,
                  marginTop: 24,
                }}
              >
                <ReactionLink
                  href={`/rating/${content.id}/like`}
                  title="좋아한 사람들"
                  count={content.stats.likeCount}
                  average={content.stats.likeAverageScore}
                />
                <ReactionLink
                  href={`/rating/${content.id}/dislike`}
                  title="싫어한 사람들"
                  count={content.stats.dislikeCount}
                  average={content.stats.dislikeAverageScore}
                />
              </div>
            </article>
          ))}
        </section>
      )}

      <Pagination currentPage={toUiPage(contents.page)} totalPages={contents.totalPages} />
    </main>
  );
}

/** 관람 등급 배지. 0이면 전체 이용가 */
export function AgeBadge({ age }: { age: number }) {
  return (
    <span
      aria-label={age === 0 ? "전체 이용가" : `${age}세 이상`}
      style={{
        padding: "2px 6px",
        borderRadius: 4,
        background: AGE_COLORS[age] ?? "#555",
        color: "#fff",
        fontSize: 11,
      }}
    >
      {age === 0 ? "ALL" : age}
    </span>
  );
}

/** 좋아요·싫어요 비율 막대. 후기가 없으면 "-"와 빈 막대 */
export function RatioBar({ stats }: { stats: SentimentStats }) {
  const likeRatio = stats.likeRatio;
  const dislikeRatio = likeRatio === null ? null : 100 - likeRatio;

  return (
    <div style={{ marginTop: 22 }}>
      <p
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "baseline",
          fontSize: 16,
          fontWeight: 700,
        }}
      >
        <span>
          좋아요 {likeRatio === null ? "-" : `${likeRatio}%`}{" "}
          <span style={{ fontSize: 11, fontWeight: 400, color: "#555" }}>
            {stats.likeCount}명
          </span>
        </span>
        <span>
          싫어요 {dislikeRatio === null ? "-" : `${dislikeRatio}%`}{" "}
          <span style={{ fontSize: 11, fontWeight: 400, color: "#555" }}>
            {stats.dislikeCount}명
          </span>
        </span>
      </p>
      <div
        aria-hidden
        style={{
          display: "flex",
          height: 8,
          marginTop: 8,
          border: "1px solid #222",
          borderRadius: 4,
          background: likeRatio === null ? "#eee" : "#ccc",
          overflow: "hidden",
        }}
      >
        <span style={{ width: `${likeRatio ?? 0}%`, background: "#222" }} />
      </div>
    </div>
  );
}

function ReactionLink({
  href,
  title,
  count,
  average,
}: {
  href: string;
  title: string;
  count: number;
  average: number | null;
}) {
  return (
    <Link
      href={href}
      style={{
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        padding: 16,
        border: "1px solid #222",
        borderRadius: 6,
        fontSize: 14,
      }}
    >
      <span>
        {title} <span style={{ fontSize: 12, color: "#777" }}>후기 {count}개 보기 ›</span>
      </span>
      <span style={{ display: "flex", alignItems: "center", gap: 8 }}>
        <Stars score={average ?? 0} size={11} />
        {formatScore(average)}
      </span>
    </Link>
  );
}
