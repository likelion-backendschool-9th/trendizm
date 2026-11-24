import Link from "next/link";
import Stars from "@/app/rating/_components/Stars";
import Pagination from "@/components/Pagination";

// 와이어프레임 목업 데이터 모양. 목록 화면을 API에 연결하면서 RatingList.tsx에서 옮겨 왔다.
// TODO(rating): 상세 화면 연결 PR에서 API 응답 타입(_lib/types.ts)으로 바꾼다.
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

type ReactionType = "like" | "dislike";

type RatingDetailProps = {
  type: ReactionType;
  contentId: string;
  content: {
    title: string;
    ageRating: number;
    status: string;
    releasedAt: string;
    season: string;
    episodeLength: string;
    category: { slug: string; name: string };
    subcategory: { slug: string; name: string };
  };
  like: Omit<RatingReaction, "reviews">;
  dislike: Omit<RatingReaction, "reviews">;
  // type 쪽 후기만 받는다.
  reviews: RatingReview[];
};

const LABELS = { like: "좋아요", dislike: "싫어요" } as const;

const PEOPLE = { like: "좋아한 사람들", dislike: "싫어한 사람들" } as const;

// 와이어프레임: 컨텐츠 별 호/불호 평점 - 상세페이지(좋아요), (싫어요) - 수정본
// /rating/[id]/like, /rating/[id]/dislike가 함께 쓴다.
export default function RatingDetail({
  type,
  contentId,
  content,
  like,
  dislike,
  reviews,
}: RatingDetailProps) {
  const reactions = { like, dislike };
  const selected = reactions[type];

  return (
    <main
      style={{
        width: "100%",
        maxWidth: 800,
        margin: "0 auto",
        padding: "28px 32px 48px",
        background: "#fff",
        color: "#111",
      }}
    >
      <nav aria-label="경로" style={{ fontSize: 12, color: "#555" }}>
        <Link href="/rating">‹ 호불호 평점</Link>
        <span style={{ margin: "0 6px" }}>/</span>
        <Link href={`/rating/cate/${content.category.slug}`}>
          {content.category.name}
        </Link>
        <span style={{ margin: "0 6px" }}>/</span>
        <Link
          href={`/rating/cate/${content.category.slug}/${content.subcategory.slug}`}
        >
          {content.subcategory.name}
        </Link>
        <span style={{ margin: "0 6px" }}>/</span>
        {content.title}
      </nav>

      <section
        aria-label="콘텐츠 기본 정보"
        style={{
          display: "flex",
          flexWrap: "wrap",
          gap: 24,
          marginTop: 22,
          padding: 24,
          border: "1px solid #111",
        }}
      >
        <div
          aria-hidden
          style={{ flex: "0 0 152px", height: 216, background: "#e5e5e5" }}
        />
        <div style={{ flex: "1 1 360px", minWidth: 0 }}>
          <h1
            style={{
              display: "flex",
              alignItems: "center",
              gap: 14,
              fontSize: 28,
              fontWeight: 700,
            }}
          >
            {content.title}
            <span
              aria-label={`${content.ageRating}세 이상`}
              style={{
                padding: "2px 6px",
                border: "1px solid #111",
                fontSize: 13,
                fontWeight: 400,
              }}
            >
              {content.ageRating}
            </span>
          </h1>
          <p
            style={{
              display: "flex",
              flexWrap: "wrap",
              alignItems: "center",
              gap: 10,
              marginTop: 12,
              fontSize: 13,
            }}
          >
            <span
              style={{
                padding: "2px 6px",
                border: "1px solid #111",
                fontSize: 11,
                fontWeight: 700,
              }}
            >
              {content.status}
            </span>
            {content.releasedAt} 공개 · {content.season} ·{" "}
            {content.episodeLength}
          </p>

          <dl
            style={{
              display: "grid",
              gridTemplateColumns: "repeat(3, minmax(0, 1fr))",
              gap: 8,
              marginTop: 14,
            }}
          >
            {[
              { label: "전체 평가", value: `${like.count + dislike.count}명` },
              { label: "좋아요 평균 별점", value: `★ ${like.averageScore.toFixed(1)}` },
              {
                label: "싫어요 평균 별점",
                value: `★ ${dislike.averageScore.toFixed(1)}`,
              },
            ].map((stat) => (
              <div
                key={stat.label}
                style={{
                  display: "flex",
                  flexDirection: "column",
                  gap: 4,
                  padding: "8px 12px 10px",
                  border: "1px solid #111",
                }}
              >
                <dt style={{ fontSize: 10, color: "#555" }}>{stat.label}</dt>
                <dd style={{ fontSize: 22, fontWeight: 700 }}>{stat.value}</dd>
              </div>
            ))}
          </dl>

          <p
            style={{
              display: "flex",
              justifyContent: "space-between",
              marginTop: 22,
              fontSize: 12,
              fontWeight: 700,
            }}
          >
            <span>
              좋아요 {like.ratio}% {like.count}명
            </span>
            <span>
              {dislike.count}명 {dislike.ratio}% 싫어요
            </span>
          </p>
          <div
            aria-hidden
            style={{
              display: "flex",
              height: 8,
              marginTop: 10,
              border: "1px solid #111",
              background: "#eee",
            }}
          >
            <span style={{ width: `${like.ratio}%`, background: "#111" }} />
          </div>
        </div>
      </section>

      <nav
        aria-label="반응 선택"
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(2, minmax(0, 1fr))",
          gap: 12,
          marginTop: 20,
        }}
      >
        {(["like", "dislike"] as const).map((reactionType) => {
          const current = reactionType === type;

          return (
            <Link
              key={reactionType}
              href={`/rating/${contentId}/${reactionType}`}
              aria-current={current ? "page" : undefined}
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                height: 52,
                padding: "0 20px",
                border: "1px solid #111",
                background: current ? "#111" : "#fff",
                color: current ? "#fff" : "#111",
                fontSize: 16,
                fontWeight: 700,
              }}
            >
              {PEOPLE[reactionType]}
              <span style={{ fontSize: 26 }}>{reactions[reactionType].ratio}%</span>
            </Link>
          );
        })}
      </nav>

      <section
        aria-label="사용자군 분석"
        style={{
          display: "flex",
          flexWrap: "wrap",
          justifyContent: "space-between",
          gap: 16,
          marginTop: 20,
          padding: "16px 20px 20px",
          border: "1px solid #111",
        }}
      >
        <div>
          <h2 style={{ fontSize: 17, fontWeight: 700 }}>
            주로 이런 사람들이 {type === "like" ? "좋아했어요" : "싫어했어요"}
          </h2>
          <ul
            aria-label="대표 사용자군"
            style={{
              display: "flex",
              flexWrap: "wrap",
              gap: 8,
              marginTop: 14,
              listStyle: "none",
            }}
          >
            {selected.tags.map((tag) => (
              <li
                key={tag.label}
                style={{
                  padding: "4px 16px",
                  border: "1px solid #999",
                  borderRadius: 999,
                  fontSize: 11,
                }}
              >
                {tag.label} {tag.value}
              </li>
            ))}
          </ul>
        </div>
        <div style={{ textAlign: "right" }}>
          <p style={{ fontSize: 10, color: "#555" }}>
            {selected.count}명의 평균 별점
          </p>
          <p
            style={{
              display: "flex",
              alignItems: "center",
              gap: 8,
              marginTop: 18,
              fontSize: 14,
            }}
          >
            <Stars score={selected.averageScore} color="#111" size={14} />
            {selected.averageScore.toFixed(1)}
          </p>
        </div>
      </section>

      <section style={{ marginTop: 28 }}>
        <header
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
          }}
        >
          <h2 style={{ fontSize: 20, fontWeight: 700 }}>
            {LABELS[type]} 후기 {reviews.length}
          </h2>
          {/* TODO: 정렬 연결 */}
          <label
            style={{
              display: "flex",
              alignItems: "center",
              gap: 24,
              fontSize: 12,
              color: "#555",
            }}
          >
            정렬
            <select
              name="sort"
              defaultValue="likes"
              style={{
                width: 120,
                height: 32,
                padding: "0 10px",
                border: "1px solid #111",
                background: "#fff",
                fontSize: 12,
              }}
            >
              <option value="likes">공감순</option>
              <option value="latest">최신순</option>
              <option value="score-desc">별점 높은 순</option>
              <option value="score-asc">별점 낮은 순</option>
            </select>
          </label>
        </header>

        <ul
          style={{
            marginTop: 22,
            padding: "0 20px",
            border: "1px solid #111",
            listStyle: "none",
          }}
        >
          {reviews.map((review, index) => (
            <li
              key={review.id}
              style={{
                display: "flex",
                gap: 12,
                padding: "18px 0",
                borderTop: index === 0 ? "none" : "1px solid #ccc",
              }}
            >
              <span
                aria-hidden
                style={{
                  flexShrink: 0,
                  width: 32,
                  height: 32,
                  borderRadius: "50%",
                  background: "#e5e5e5",
                }}
              />
              <div style={{ flex: 1, minWidth: 0 }}>
                <p style={{ fontSize: 11 }}>
                  {review.author.nickname}{" "}
                  <span style={{ marginLeft: 4, color: "#777" }}>
                    {review.author.tags.join(" · ")}
                  </span>
                </p>
                <p
                  style={{
                    display: "flex",
                    alignItems: "center",
                    gap: 12,
                    marginTop: 6,
                  }}
                >
                  <Stars score={review.score} color="#111" size={13} />
                  <span
                    style={{
                      padding: "1px 8px",
                      border: "1px solid #111",
                      fontSize: 11,
                    }}
                  >
                    {review.watched}
                  </span>
                </p>
                <p style={{ marginTop: 12, fontSize: 17, fontWeight: 700 }}>
                  {review.title}
                </p>
                <p style={{ marginTop: 6, fontSize: 14 }}>{review.body}</p>
                <div
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    marginTop: 14,
                  }}
                >
                  <time style={{ fontSize: 11, color: "#777" }}>
                    {review.createdAt}
                  </time>
                  {/* TODO: 공감 요청 연결 */}
                  <button
                    type="button"
                    style={{
                      padding: "4px 12px",
                      border: "1px solid #111",
                      background: "#fff",
                      fontSize: 12,
                      fontWeight: 700,
                      cursor: "pointer",
                    }}
                  >
                    ♡ 공감 {review.likeCount}
                  </button>
                </div>
              </div>
            </li>
          ))}
        </ul>

        {/* TODO: 실제 현재 페이지와 전체 페이지 수 연결 */}
        <Pagination currentPage={1} totalPages={10} />
      </section>

      <section
        style={{ marginTop: 28, padding: "24px 24px 26px", border: "1px solid #111" }}
      >
        <h2 style={{ fontSize: 19, fontWeight: 700 }}>후기 남기기</h2>
        <p style={{ marginTop: 10, fontSize: 12, color: "#555" }}>
          평가와 후기를 함께 남겨 주세요.
        </p>

        {/* TODO: 후기 등록 요청 연결 */}
        <form style={{ marginTop: 24 }}>
          <fieldset style={{ border: "none" }}>
            <legend style={{ fontSize: 14, fontWeight: 700 }}>
              이 콘텐츠, 어땠나요?
            </legend>
            <div
              style={{
                display: "grid",
                gridTemplateColumns: "repeat(2, minmax(0, 1fr))",
                gap: 16,
                marginTop: 12,
              }}
            >
              {(["like", "dislike"] as const).map((reactionType) => {
                const checked = reactionType === type;

                return (
                  <label
                    key={reactionType}
                    style={{
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      height: 40,
                      border: "1px solid #111",
                      background: checked ? "#222" : "#fff",
                      color: checked ? "#fff" : "#111",
                      fontSize: 13,
                      fontWeight: 700,
                      cursor: "pointer",
                    }}
                  >
                    {/* 라디오는 숨기고 라벨 색으로 선택을 보여 준다. */}
                    {/* TODO: 지금은 페이지 종류(like/dislike)로만 색이 정해진다. 누른 쪽으로 바뀌게 연결 */}
                    <input
                      type="radio"
                      name="reaction"
                      value={reactionType}
                      defaultChecked={checked}
                      style={{ position: "absolute", opacity: 0 }}
                    />
                    {LABELS[reactionType]}
                  </label>
                );
              })}
            </div>
          </fieldset>

          <div
            style={{
              display: "grid",
              gridTemplateColumns: "repeat(2, minmax(0, 1fr))",
              gap: 16,
              marginTop: 30,
            }}
          >
            <fieldset style={{ border: "none" }}>
              <legend style={{ fontSize: 13, fontWeight: 700 }}>별점</legend>
              {/* TODO: 별을 눌러 점수 선택 */}
              <div
                style={{
                  display: "flex",
                  alignItems: "center",
                  gap: 16,
                  marginTop: 12,
                }}
              >
                <span aria-hidden style={{ fontSize: 24, letterSpacing: 1 }}>
                  ☆☆☆☆☆
                </span>
                <span style={{ fontSize: 12, color: "#777" }}>
                  별을 눌러 주세요
                </span>
              </div>
            </fieldset>
            <label style={{ fontSize: 13, fontWeight: 700 }}>
              시청 회차
              <span
                style={{
                  display: "flex",
                  alignItems: "center",
                  gap: 16,
                  marginTop: 12,
                  fontSize: 12,
                  fontWeight: 400,
                }}
              >
                <input
                  type="number"
                  name="watched"
                  min={1}
                  placeholder="2"
                  style={{
                    width: 76,
                    height: 36,
                    padding: "0 12px",
                    border: "1px solid #999",
                    fontSize: 13,
                  }}
                />
                화까지 시청
              </span>
            </label>
          </div>

          <label
            style={{
              display: "flex",
              flexDirection: "column",
              gap: 10,
              marginTop: 28,
              fontSize: 13,
              fontWeight: 700,
            }}
          >
            한줄 제목
            <input
              type="text"
              name="title"
              placeholder="한줄 제목을 입력해 주세요"
              style={{
                height: 40,
                padding: "0 12px",
                border: "1px solid #999",
                fontSize: 13,
                fontWeight: 400,
              }}
            />
          </label>

          <label
            style={{
              display: "flex",
              flexDirection: "column",
              gap: 10,
              marginTop: 24,
              fontSize: 13,
              fontWeight: 700,
            }}
          >
            후기
            <textarea
              name="body"
              maxLength={500}
              placeholder="이 콘텐츠에 대한 생각을 자유롭게 적어 주세요."
              style={{
                height: 120,
                padding: 12,
                border: "1px solid #999",
                resize: "vertical",
                fontSize: 13,
                fontWeight: 400,
              }}
            />
          </label>
          {/* TODO: 입력한 글자 수로 갱신 */}
          <p style={{ marginTop: 8, fontSize: 11, color: "#777", textAlign: "right" }}>
            0 / 500
          </p>

          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              marginTop: 16,
              paddingTop: 16,
              borderTop: "1px solid #ccc",
            }}
          >
            <p style={{ fontSize: 12, color: "#555" }}>
              후기를 등록하면 포인트가 적립돼요
            </p>
            <button
              type="submit"
              style={{
                width: 104,
                height: 38,
                border: "none",
                background: "#111",
                color: "#fff",
                fontSize: 13,
                fontWeight: 700,
                cursor: "pointer",
              }}
            >
              후기 등록
            </button>
          </div>
        </form>
      </section>

      <Link
        href="/rating"
        style={{
          display: "block",
          width: 120,
          margin: "20px auto 0",
          padding: "8px 0",
          border: "1px solid #111",
          textAlign: "center",
          fontSize: 14,
        }}
      >
        ☰ 목록으로
      </Link>
    </main>
  );
}
