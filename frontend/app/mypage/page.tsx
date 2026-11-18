import Link from "next/link";
import SavedContentCard, {
  type SavedContent,
} from "@/app/mypage/_components/SavedContentCard";

// 와이어프레임: 마이페이지

const PROFILE = {
  name: "홍길동",
  email: "user@example.com",
  joinedAt: "2026.01.01",
};

const SUMMARY = [
  { label: "보유 포인트", value: "1,200 P" },
  { label: "저장한 콘텐츠", value: "12개" },
  { label: "작성한 댓글", value: "8개" },
];

const RECENT_ACTIVITIES = [
  {
    id: 1,
    title: "저장한 트렌드",
    body: "최근 관심 콘텐츠를 저장했습니다.",
    date: "2026.11.14",
  },
  {
    id: 2,
    title: "작성한 댓글",
    body: "콘텐츠에 새 의견을 남겼습니다.",
    date: "2026.11.12",
  },
  {
    id: 3,
    title: "포인트 적립",
    body: "활동 참여로 포인트를 받았습니다.",
    date: "2026.11.10",
  },
];

const SAVED_CONTENTS: SavedContent[] = [
  {
    type: "trend",
    id: 1,
    category: "양말",
    title: "양말 추천해주세요ㅠㅠ",
    summary:
      "세련된 스타일의 화이트 계열 양말을 찾고 있어요. 따뜻하고 폭신한 소재를 선호합니다.",
    commentCount: 2,
    recommendedItem: {
      image: "/images/item/socks.jpg",
      name: "하얀색 양말",
      price: "10,000원",
    },
  },
  {
    type: "rating",
    id: 1,
    title: "컨텐츠 제목",
    meta: "2026.10.28 · 방영중 · 시즌 2",
    likeRatio: 72,
    dislikeRatio: 28,
  },
];

const SETTINGS = [
  {
    name: "개인정보 관리",
    value: "이메일 · 비밀번호",
    href: "/mypage/privacy",
  },
  { name: "알림 설정", value: "서비스 알림 관리", href: "/mypage/privacy" },
  // TODO: 고객 지원 화면이 생기면 링크 연결
  { name: "고객 지원", value: "문의 및 도움말" },
];

export default function MypagePage() {
  return (
    <main style={{ display: "flex", flexDirection: "column", gap: 24 }}>
      <section
        aria-label="프로필 요약"
        style={{
          display: "flex",
          flexWrap: "wrap",
          alignItems: "center",
          gap: 28,
          padding: "28px 28px",
          border: "1px solid #999",
        }}
      >
        <div
          aria-hidden
          style={{
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            width: 104,
            height: 104,
            borderRadius: "50%",
            background: "#ddd",
            fontSize: 13,
            color: "#555",
          }}
        >
          이미지
        </div>
        <div style={{ flex: 1 }}>
          <h1 style={{ fontSize: 22, fontWeight: 700 }}>{PROFILE.name}</h1>
          <p style={{ marginTop: 14, fontSize: 14, color: "#555" }}>
            {PROFILE.email}
          </p>
          <p style={{ marginTop: 14, fontSize: 12, color: "#555" }}>
            가입일 {PROFILE.joinedAt}
          </p>
        </div>
        {/* TODO: 프로필 편집 연결 */}
        <button
          type="button"
          style={{
            width: 128,
            height: 42,
            border: "1px solid #999",
            background: "#fff",
            fontSize: 13,
            fontWeight: 700,
            cursor: "pointer",
          }}
        >
          프로필 편집
        </button>
      </section>

      <section aria-label="활동 요약" style={{ marginTop: 8 }}>
        <dl
          style={{
            display: "grid",
            gridTemplateColumns: "repeat(auto-fit, minmax(160px, 1fr))",
            gap: 16,
          }}
        >
          {SUMMARY.map((item) => (
            <div
              key={item.label}
              style={{
                display: "flex",
                flexDirection: "column-reverse",
                gap: 24,
                padding: "24px 20px 34px",
                background: "#f2f2f2",
              }}
            >
              <dt style={{ fontSize: 13, color: "#555" }}>{item.label}</dt>
              <dd style={{ fontSize: 24, fontWeight: 700 }}>{item.value}</dd>
            </div>
          ))}
        </dl>
      </section>

      <section
        style={{
          marginTop: 18,
          padding: "20px 24px 42px",
          border: "1px solid #999",
        }}
      >
        <header
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
          }}
        >
          <h2 style={{ fontSize: 20, fontWeight: 700 }}>최근 활동</h2>
          {/* TODO: 최근 활동 전체 화면이 생기면 링크 연결 */}
          <span style={{ paddingRight: 20, fontSize: 12 }}>전체 보기 →</span>
        </header>
        <ul
          style={{
            display: "flex",
            flexDirection: "column",
            gap: 10,
            marginTop: 22,
            listStyle: "none",
          }}
        >
          {RECENT_ACTIVITIES.map((activity) => (
            <li
              key={activity.id}
              style={{
                position: "relative",
                minHeight: 96,
                padding: "20px 16px",
                border: "1px solid #999",
              }}
            >
              <p style={{ fontSize: 14, fontWeight: 700 }}>{activity.title}</p>
              <p style={{ marginTop: 14, fontSize: 13, color: "#555" }}>
                {activity.body}
              </p>
              <time
                style={{
                  position: "absolute",
                  top: 16,
                  right: 56,
                  fontSize: 11,
                  color: "#555",
                }}
              >
                {activity.date}
              </time>
            </li>
          ))}
        </ul>
      </section>

      <section style={{ padding: "20px 24px 20px", border: "1px solid #999" }}>
        <header
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
          }}
        >
          <h2 style={{ fontSize: 20, fontWeight: 700 }}>저장한 콘텐츠</h2>
          <Link href="/mypage/saved" style={{ paddingRight: 20, fontSize: 12 }}>
            전체 보기 →
          </Link>
        </header>
        <div
          style={{
            display: "flex",
            flexDirection: "column",
            gap: 12,
            marginTop: 22,
          }}
        >
          {SAVED_CONTENTS.map((content) => (
            <SavedContentCard
              key={`${content.type}-${content.id}`}
              content={content}
            />
          ))}
        </div>
      </section>

      <section style={{ padding: "20px 24px 20px", border: "1px solid #999" }}>
        <h2 style={{ fontSize: 20, fontWeight: 700 }}>계정 설정</h2>
        <ul
          style={{
            display: "flex",
            flexDirection: "column",
            gap: 6,
            marginTop: 22,
            listStyle: "none",
          }}
        >
          {SETTINGS.map((setting) => {
            const row = (
              <>
                <span style={{ width: 204, fontSize: 14 }}>{setting.name}</span>
                <span style={{ flex: 1, fontSize: 12, color: "#555" }}>
                  {setting.value}
                </span>
                <span aria-hidden style={{ fontSize: 18 }}>
                  ›
                </span>
              </>
            );
            const rowStyle = {
              display: "flex",
              alignItems: "center",
              height: 60,
              padding: "0 18px 0 16px",
              border: "1px solid #999",
            };

            return (
              <li key={setting.name}>
                {setting.href ? (
                  <Link href={setting.href} style={rowStyle}>
                    {row}
                  </Link>
                ) : (
                  <div style={rowStyle}>{row}</div>
                )}
              </li>
            );
          })}
        </ul>
      </section>
    </main>
  );
}
