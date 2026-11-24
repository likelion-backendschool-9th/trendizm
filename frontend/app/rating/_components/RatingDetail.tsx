import Link from "next/link";
import RatingImage from "@/app/rating/_components/RatingImage";
import { AgeBadge, RatioBar } from "@/app/rating/_components/RatingList";
import ReviewArea from "@/app/rating/_components/ReviewArea";
import Stars from "@/app/rating/_components/Stars";
import { PROGRESS_SUBCATEGORIES } from "@/app/rating/_lib/categories";
import { extraLabels, formatDate, formatScore } from "@/app/rating/_lib/format";
import type { RatingContentDetail, RatingReviewItem, Sentiment } from "@/app/rating/_lib/types";
import type { PageResponse } from "@/lib/api/types";

type RatingDetailProps = {
  content: RatingContentDetail;
  sentiment: Sentiment;
  reviews: PageResponse<RatingReviewItem>;
};

const SENTIMENT_NAME: Record<Sentiment, string> = { like: "좋아요", dislike: "싫어요" };

// 와이어프레임: 컨텐츠 별 호/불호 평점 - 상세 (rating/[id]/like, rating/[id]/dislike)
// 콘텐츠 정보, 좋아요·싫어요 탭, 고른 쪽의 집계와 후기 목록·작성 폼을 보여 준다.
// 와이어프레임의 사용자군 분석(연령·MBTI 등 TOP5)은 회원 프로필 조회(findProfiles)가 생기면 붙인다.
// 와이어프레임의 상세 댓글 작성은 ERD에서 빠져서 넣지 않는다.
export default function RatingDetail({ content, sentiment, reviews }: RatingDetailProps) {
  const extras = extraLabels(content.extra);
  const count = sentiment === "like" ? content.stats.likeCount : content.stats.dislikeCount;
  const average =
    sentiment === "like" ? content.stats.likeAverageScore : content.stats.dislikeAverageScore;

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
      <nav aria-label="경로" style={{ fontSize: 12, color: "#555" }}>
        <Link href="/rating">평점</Link>
        <span style={{ margin: "0 6px" }}>&gt;</span>
        <Link href={`/rating/cate/${content.category.slug}`}>{content.category.label}</Link>
        <span style={{ margin: "0 6px" }}>&gt;</span>
        <Link
          href={`/rating/cate/${content.category.slug}/${content.subcategory.slug}`}
          style={{ fontWeight: 700, color: "#111" }}
        >
          {content.subcategory.label}
        </Link>
      </nav>

      <section
        aria-label="콘텐츠 기본 정보"
        style={{
          display: "flex",
          gap: 24,
          marginTop: 16,
          padding: 24,
          border: "1px solid #222",
          borderRadius: 12,
        }}
      >
        <RatingImage src={content.imageUrl} alt={content.title} width={140} height={200} />
        <div style={{ flex: 1, minWidth: 0 }}>
          <h1
            style={{
              display: "flex",
              alignItems: "center",
              gap: 14,
              fontSize: 26,
              fontWeight: 700,
            }}
          >
            {content.title}
            {content.ageRating !== null && <AgeBadge age={content.ageRating} />}
          </h1>
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
            {extras.map((label) => (
              <span key={label}>{label}</span>
            ))}
          </p>
          {content.description && (
            <p
              style={{
                marginTop: 14,
                fontSize: 14,
                lineHeight: 1.6,
                whiteSpace: "pre-wrap",
              }}
            >
              {content.description}
            </p>
          )}
          <RatioBar stats={content.stats} />
        </div>
      </section>

      <nav
        aria-label="반응 선택"
        style={{ display: "flex", gap: 8, marginTop: 24 }}
      >
        {(["like", "dislike"] as const).map((option) => {
          const selected = option === sentiment;
          const optionCount =
            option === "like" ? content.stats.likeCount : content.stats.dislikeCount;
          return (
            <Link
              key={option}
              href={`/rating/${content.id}/${option}`}
              aria-current={selected ? "page" : undefined}
              style={{
                flex: 1,
                padding: "12px 0",
                border: "1px solid #222",
                borderRadius: 8,
                background: selected ? "#111" : "#fff",
                color: selected ? "#fff" : "#111",
                textAlign: "center",
                fontSize: 14,
                fontWeight: 700,
              }}
            >
              {option === "like" ? "👍" : "👎"} {SENTIMENT_NAME[option]} {optionCount}
            </Link>
          );
        })}
      </nav>

      <section
        aria-label={`${SENTIMENT_NAME[sentiment]}한 사람들`}
        style={{
          display: "flex",
          flexWrap: "wrap",
          justifyContent: "space-between",
          alignItems: "center",
          gap: 12,
          marginTop: 16,
          padding: 20,
          border: "1px solid #222",
          borderRadius: 12,
        }}
      >
        <div>
          <h2 style={{ fontSize: 17, fontWeight: 700 }}>
            {SENTIMENT_NAME[sentiment]}한 사람들 {count}명
          </h2>
          <p style={{ marginTop: 6, fontSize: 12, color: "#777" }}>
            어떤 사람들이 {SENTIMENT_NAME[sentiment]}했는지(연령대 등) 분석은 준비 중이에요
          </p>
        </div>
        <p style={{ display: "flex", alignItems: "center", gap: 8, fontSize: 18, fontWeight: 700 }}>
          <Stars score={average ?? 0} color="#111" size={16} />
          {formatScore(average)}
        </p>
      </section>

      <ReviewArea
        // 탭을 바꾸면 목록·폼 상태를 새로 시작한다
        key={`${content.id}-${sentiment}`}
        contentId={content.id}
        sentiment={sentiment}
        initialPage={reviews}
        askProgress={PROGRESS_SUBCATEGORIES.has(content.subcategory.slug)}
      />
    </main>
  );
}
