import Pagination from "@/components/Pagination";
import SavedContentCard, {
  type SavedContent,
} from "@/app/mypage/_components/SavedContentCard";

// 와이어프레임: 저장한 콘텐츠 (mypage/saved)

const TOTAL_COUNT = 12;

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
  {
    type: "trend",
    id: 2,
    category: "양말",
    title: "화이트 양말 코디 트렌드",
    summary: "최근 인기 있는 양말 스타일링과 상품을 모아봤어요.",
    commentCount: 2,
    recommendedItem: {
      image: "/images/item/socks.jpg",
      name: "하얀색 양말",
      price: "10,000원",
    },
  },
];

const FILTERS = ["전체", "트렌드 아이템", "호불호 평점"];

export default function SavedPage() {
  return (
    <main style={{ display: "flex", flexDirection: "column", gap: 24 }}>
      <header>
        <h1 style={{ fontSize: 26, fontWeight: 700 }}>저장한 콘텐츠</h1>
        <p style={{ marginTop: 14, fontSize: 13, color: "#555" }}>
          관심 있는 트렌드와 평점 콘텐츠를 다시 확인할 수 있습니다.
        </p>
      </header>

      <section
        aria-label="저장 요약"
        style={{ marginTop: 12, padding: "20px 20px 22px", background: "#f2f2f2" }}
      >
        <h2 style={{ fontSize: 18, fontWeight: 700 }}>
          저장한 콘텐츠 {TOTAL_COUNT}개
        </h2>
        <p style={{ marginTop: 12, fontSize: 12, color: "#555" }}>
          저장한 순서대로 표시됩니다. 콘텐츠를 선택하면 상세 화면으로
          이동합니다.
        </p>
      </section>

      {/* TODO: 필터, 정렬 연결 */}
      <div
        style={{
          display: "flex",
          flexWrap: "wrap",
          alignItems: "center",
          gap: 8,
          padding: 16,
          border: "1px solid #999",
        }}
      >
        {FILTERS.map((filter, index) => (
          <button
            key={filter}
            type="button"
            aria-pressed={index === 0}
            style={{
              minWidth: 72,
              height: 36,
              padding: "0 12px",
              border: "1px solid #999",
              background: index === 0 ? "#f2f2f2" : "#fff",
              fontSize: 13,
              fontWeight: index === 0 ? 700 : 400,
              textAlign: "left",
              cursor: "pointer",
            }}
          >
            {filter}
          </button>
        ))}
        <select
          name="sort"
          defaultValue="latest"
          style={{
            marginLeft: "auto",
            border: "none",
            background: "none",
            color: "#555",
            fontSize: 13,
          }}
        >
          <option value="latest">최신 저장순</option>
        </select>
      </div>

      <section style={{ padding: "24px 22px 72px", border: "1px solid #999" }}>
        <header
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
          }}
        >
          <h2 style={{ fontSize: 18, fontWeight: 700 }}>저장 목록</h2>
          <p style={{ fontSize: 12, color: "#555" }}>{TOTAL_COUNT}개</p>
        </header>

        <div
          style={{
            display: "flex",
            flexDirection: "column",
            gap: 12,
            marginTop: 16,
          }}
        >
          {SAVED_CONTENTS.map((content) => (
            <SavedContentCard
              key={`${content.type}-${content.id}`}
              content={content}
            />
          ))}
        </div>

        <p
          style={{
            marginTop: 14,
            fontSize: 12,
            color: "#555",
            textAlign: "center",
          }}
        >
          1–{SAVED_CONTENTS.length} / {TOTAL_COUNT}개
        </p>
        {/* TODO: 실제 현재 페이지 연결 */}
        <Pagination
          currentPage={1}
          totalPages={Math.ceil(TOTAL_COUNT / SAVED_CONTENTS.length)}
        />
      </section>
    </main>
  );
}
