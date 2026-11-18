import Link from "next/link";
import RatingCategoryNav from "@/app/rating/_components/RatingCategoryNav";
import Stars from "@/app/rating/_components/Stars";
import Pagination from "@/components/Pagination";

export type RatingCategory = {
  slug: string;
  name: string;
  // 대분류 칸에 보이는 아이콘. public 폴더 기준 SVG 경로
  icon: string;
  subcategories: { slug: string; name: string }[];
};

export type RatingReview = {
  id: number;
  title: string;
  score: number;
  watched: string;
  body: string;
  createdAt: string;
  likeCount: number;
  author: { id: string; nickname: string; tags: string[] };
};

export type RatingReaction = {
  count: number;
  ratio: number;
  averageScore: number;
  // 이 반응을 주로 보인 사용자군. 예: { label: "연령", value: "20대" }
  tags: { label: string; value: string }[];
  reviews: RatingReview[];
};

export type RatingContent = {
  id: number;
  title: string;
  category: string;
  subcategory: string;
  ageRating: number;
  status: string;
  releasedAt: string;
  season: string;
  episodeLength: string;
  like: RatingReaction;
  dislike: RatingReaction;
};

type RatingListProps = {
  categories: RatingCategory[];
  cate?: string;
  subcate?: string;
  totalCount: number;
  contents: RatingContent[];
};

// 관람 등급 배지 색
const AGE_COLORS: Record<number, string> = {
  19: "#a33",
  15: "#8a5a00",
  12: "#2f7a3a",
};

// 와이어프레임: 컨텐츠 별 호/불호 평점 (rating) - 수정본
// /rating, /rating/cate/[cate], /rating/cate/[cate]/[subcate]가 함께 쓴다.
export default function RatingList({
  categories,
  cate,
  subcate,
  totalCount,
  contents,
}: RatingListProps) {
  const current =
    categories.find((category) => category.slug === cate) ?? categories[0];
  const currentSub =
    current.subcategories.find((subcategory) => subcategory.slug === subcate)
      ?.name ?? "전체";

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

      <RatingCategoryNav categories={categories} cate={cate} subcate={subcate} />

      {/* TODO: 정렬 연결 */}
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
          {current.name} &gt; {currentSub}{" "}
          <strong style={{ marginLeft: 4 }}>{totalCount}개</strong>
        </p>
        <label
          style={{
            display: "flex",
            alignItems: "center",
            gap: 8,
            marginLeft: "auto",
            fontSize: 12,
          }}
        >
          정렬
          <select
            name="sort"
            defaultValue="latest"
            style={{
              padding: "5px 8px",
              border: "1px solid #222",
              borderRadius: 6,
              background: "#fff",
              fontSize: 12,
            }}
          >
            <option value="latest">최신순</option>
            <option value="reviews">리뷰순</option>
            <option value="likes">좋아요 많은 순</option>
            <option value="dislikes">싫어요 많은 순</option>
            <option value="score">평점 순</option>
          </select>
        </label>
      </div>

      <section
        aria-label="콘텐츠 목록"
        style={{ display: "flex", flexDirection: "column", gap: 24 }}
      >
        {contents.map((content) => (
          <article
            key={content.id}
            style={{ padding: 24, border: "1px solid #222", borderRadius: 12 }}
          >
            <header style={{ display: "flex", gap: 24 }}>
              <div
                aria-hidden
                style={{
                  flex: "0 0 112px",
                  height: 160,
                  borderRadius: 6,
                  background: "#ddd",
                }}
              />
              <div style={{ flex: 1, minWidth: 0 }}>
                <p style={{ fontSize: 12 }}>
                  {content.category} &gt; {content.subcategory}
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
                  {content.title}
                  <span
                    aria-label={`${content.ageRating}세 이상`}
                    style={{
                      padding: "2px 6px",
                      borderRadius: 4,
                      background: AGE_COLORS[content.ageRating] ?? "#555",
                      color: "#fff",
                      fontSize: 11,
                    }}
                  >
                    {content.ageRating}
                  </span>
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
                    {content.status}
                  </span>
                  <span>{content.releasedAt} 공개</span>
                  <span>{content.season}</span>
                  <span>{content.episodeLength}</span>
                </p>
                <RatioBar like={content.like} dislike={content.dislike} />
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
              <ReactionPanel contentId={content.id} type="like" reaction={content.like} />
              <ReactionPanel
                contentId={content.id}
                type="dislike"
                reaction={content.dislike}
              />
            </div>

            <Link
              href={`/rating/${content.id}/like`}
              style={{
                display: "block",
                width: 220,
                margin: "24px 0 0 auto",
                padding: "9px 0",
                border: "1px solid #222",
                borderRadius: 6,
                textAlign: "center",
                fontSize: 13,
                fontWeight: 700,
              }}
            >
              리뷰 전체 보기 ({content.like.count + content.dislike.count})
            </Link>
          </article>
        ))}
      </section>

      {/* TODO: 실제 현재 페이지와 전체 페이지 수 연결 */}
      <Pagination currentPage={1} totalPages={10} />
    </main>
  );
}

function RatioBar({
  like,
  dislike,
}: {
  like: RatingReaction;
  dislike: RatingReaction;
}) {
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
          좋아요 {like.ratio}%{" "}
          <span style={{ fontSize: 11, fontWeight: 400, color: "#555" }}>
            {like.count}명
          </span>
        </span>
        <span>
          싫어요 {dislike.ratio}%{" "}
          <span style={{ fontSize: 11, fontWeight: 400, color: "#555" }}>
            {dislike.count}명
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
          background: "#ccc",
          overflow: "hidden",
        }}
      >
        <span style={{ width: `${like.ratio}%`, background: "#222" }} />
      </div>
    </div>
  );
}

function ReactionPanel({
  contentId,
  type,
  reaction,
}: {
  contentId: number;
  type: "like" | "dislike";
  reaction: RatingReaction;
}) {
  return (
    <section style={{ padding: 16, border: "1px solid #222", borderRadius: 6 }}>
      <header
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
        }}
      >
        <h3 style={{ fontSize: 14, fontWeight: 400 }}>
          <Link href={`/rating/${contentId}/${type}`}>
            {type === "like" ? "좋아한 사람들" : "싫어한 사람들"}
          </Link>
        </h3>
        <p style={{ display: "flex", alignItems: "center", gap: 8, fontSize: 14 }}>
          <Stars score={reaction.averageScore} size={11} />
          {reaction.averageScore.toFixed(1)}
        </p>
      </header>

      <ul
        aria-label="대표 사용자군"
        style={{
          display: "flex",
          flexWrap: "wrap",
          gap: 6,
          marginTop: 14,
          listStyle: "none",
        }}
      >
        {reaction.tags.map((tag) => (
          <li
            key={tag.label}
            style={{
              padding: "3px 8px",
              border: "1px solid #222",
              borderRadius: 4,
              fontSize: 11,
            }}
          >
            {tag.value}
          </li>
        ))}
      </ul>

      <ul
        aria-label="후기"
        style={{
          display: "flex",
          flexDirection: "column",
          gap: 12,
          marginTop: 20,
          listStyle: "none",
        }}
      >
        {reaction.reviews.map((review) => (
          <li
            key={review.id}
            style={{ padding: "14px 12px", border: "1px solid #222", borderRadius: 6 }}
          >
            <div style={{ display: "flex", gap: 8 }}>
              <span
                aria-hidden
                style={{
                  flexShrink: 0,
                  width: 28,
                  height: 28,
                  border: "1px solid #ddd",
                  borderRadius: "50%",
                  background: "#f5f5f5",
                }}
              />
              <div>
                <p style={{ fontSize: 13, fontWeight: 700 }}>
                  {review.author.nickname}{" "}
                  <span style={{ fontSize: 11, fontWeight: 400, color: "#777" }}>
                    @{review.author.id}
                  </span>
                </p>
                <p style={{ marginTop: 2, fontSize: 10, color: "#555" }}>
                  {review.author.tags.join(" · ")}
                </p>
              </div>
            </div>
            <p
              style={{
                display: "flex",
                alignItems: "center",
                gap: 10,
                marginTop: 10,
                fontSize: 11,
              }}
            >
              <Stars score={review.score} size={10} />
              {review.score.toFixed(1)}
              <strong style={{ marginLeft: 8, fontSize: 14 }}>{review.title}</strong>
            </p>
            <p style={{ marginTop: 12, fontSize: 12, lineHeight: 1.5 }}>
              {review.body}
            </p>
            <p
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                marginTop: 12,
                fontSize: 11,
              }}
            >
              <time>{review.createdAt}</time>
              <span
                style={{
                  padding: "1px 8px",
                  border: "1px solid #222",
                  borderRadius: 3,
                  fontSize: 10,
                }}
              >
                {review.watched}
              </span>
            </p>
          </li>
        ))}
      </ul>
    </section>
  );
}
