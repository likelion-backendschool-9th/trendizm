import Link from "next/link";

export type FeedVideo = {
  id: number;
  title: string;
  platform: string;
  duration: string;
  // 채널, 조회수, 게시 시점 등을 · 로 이어 보여 준다.
  meta: string[];
};

type VideoFeedProps = {
  // long: 16:9 썸네일 4열, short: 세로형 썸네일 6열
  variant: "long" | "short";
  keywords: string[];
  videos: FeedVideo[];
};

// 키워드 목록은 아래로 갈수록 흐려진다.
// 줄 수는 화면 폭에 따라 바뀌므로 항목 순서가 아니라 목록 전체에 세로 그라데이션을 씌운다.
const KEYWORD_FADE = "linear-gradient(to bottom, #000 0%, #000 30%, rgba(0, 0, 0, 0.15) 100%)";

// 와이어프레임: 피드 (롱폼), 피드 (숏폼)
// /feed/long, /feed/short가 함께 쓴다.
export default function VideoFeed({ variant, keywords, videos }: VideoFeedProps) {
  const isLong = variant === "long";

  return (
    <main
      style={{
        width: "100%",
        maxWidth: 1440,
        margin: "0 auto",
        padding: "20px 20px 40px",
        background: "#fff",
        color: "#111",
      }}
    >
      <section>
        <h1
          style={{
            display: "flex",
            alignItems: "center",
            gap: 10,
            fontSize: 30,
            fontWeight: 700,
          }}
        >
          알고리즘 키워드
          <Link
            href="/mypage/clusters"
            aria-label="키워드 설정"
            style={{ fontSize: 22, fontWeight: 400 }}
          >
            ⚙
          </Link>
        </h1>
        <ul
          style={{
            display: "flex",
            flexWrap: "wrap",
            gap: "6px 5px",
            marginTop: 20,
            listStyle: "none",
            maskImage: KEYWORD_FADE,
            WebkitMaskImage: KEYWORD_FADE,
          }}
        >
          {keywords.map((keyword) => (
            <li
              key={keyword}
              style={{
                padding: "4px 16px",
                border: "2px solid #111",
                borderRadius: 999,
                fontSize: 14,
                fontWeight: 700,
              }}
            >
              {keyword}
            </li>
          ))}
        </ul>
      </section>

      <section aria-label={isLong ? "롱폼 피드" : "숏폼 피드"} style={{ marginTop: 52 }}>
        <ul
          style={{
            display: "grid",
            gridTemplateColumns: `repeat(auto-fill, minmax(${isLong ? 280 : 180}px, 1fr))`,
            gap: isLong ? "24px 24px" : "24px 16px",
            padding: "0 12px",
            listStyle: "none",
          }}
        >
          {videos.map((video) => (
            <li key={video.id}>
              <div
                style={{
                  position: "relative",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  aspectRatio: isLong ? "16 / 9" : "9 / 16",
                  background: "#ddd",
                }}
              >
                <span aria-hidden style={{ fontSize: 30, color: "#666" }}>
                  ▶
                </span>
                <span
                  style={{
                    position: "absolute",
                    top: 10,
                    right: 10,
                    width: 100,
                    padding: "4px 0",
                    borderRadius: 999,
                    background: "#fff",
                    textAlign: "center",
                    fontSize: 10,
                  }}
                >
                  {video.platform}
                </span>
                <span
                  style={{
                    position: "absolute",
                    right: 10,
                    bottom: 10,
                    padding: "3px 10px",
                    border: "1px solid #111",
                    background: "#fff",
                    fontSize: 9,
                  }}
                >
                  {video.duration}
                </span>
              </div>
              <p style={{ marginTop: 14, fontSize: 14, fontWeight: 700 }}>
                {video.title}
              </p>
              <p style={{ marginTop: 12, fontSize: 11, color: "#777" }}>
                {video.meta.join(" · ")}
              </p>
            </li>
          ))}
        </ul>
      </section>

      {/* TODO: '더보기'로 다음 10개 */}
      <button
        type="button"
        aria-label="더보기"
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          width: 120,
          height: 40,
          margin: "40px auto 0",
          border: "1px solid #999",
          background: "#fff",
          color: "#555",
          fontSize: 14,
          cursor: "pointer",
        }}
      >
        <span aria-hidden>▼</span>
      </button>
    </main>
  );
}
