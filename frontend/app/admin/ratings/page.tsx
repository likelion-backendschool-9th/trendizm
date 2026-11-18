import AdminPage from "@/app/admin/_components/AdminPage";
import Pagination from "@/components/Pagination";
import {
  AdminDetail,
  AdminFilters,
  AdminHeading,
  AdminNote,
  AdminSearch,
  AdminTable,
} from "@/app/admin/_components/AdminParts";

// 와이어프레임: 관리자 · 호불호 평점 콘텐츠 관리 (admin/ratings)

// 호불호 평점의 대분류
const CATEGORIES = ["미디어", "푸드", "음악", "도서·웹툰", "게임", "장소"];

const CONTENTS = [
  { id: 1, title: "예시 영화 A", category: "미디어/영화", likeCount: 128, dislikeCount: 45, reviewCount: 23, status: "공개" },
  { id: 2, title: "예시 음료 B", category: "푸드/음료", likeCount: 72, dislikeCount: 18, reviewCount: 11, status: "공개" },
  { id: 3, title: "예시 게임 C", category: "게임", likeCount: 38, dislikeCount: 22, reviewCount: 8, status: "숨김" },
];

// 참여자 수(좋아요+싫어요) 상위 몇 개까지 순위별로 탐색할지
const RANKING_LIMIT = 10;

// 탐색 중인 콘텐츠에서 좋아요·싫어요를 누른 회원의 프로필 특성 상위 5개
const TOP_TRAITS = {
  like: [
    "연령 · 20대",
    "MBTI · ENFP",
    "관심사 · 여행",
    "직업 · 사무직",
    "취향 · 영화",
  ],
  dislike: [
    "연령 · 30대",
    "MBTI · INFJ",
    "관심사 · 독서",
    "직업 · 학생",
    "취향 · 드라마",
  ],
};

export default function AdminRatingsPage() {
  const [selected] = CONTENTS;
  // 전체 콘텐츠를 참여자 수 내림차순으로 줄 세운 상위 목록
  const ranked = CONTENTS.map((content) => ({
    ...content,
    participantCount: content.likeCount + content.dislikeCount,
  }))
    .sort((a, b) => b.participantCount - a.participantCount)
    .slice(0, RANKING_LIMIT);
  // TODO: 이전·다음 순위 버튼으로 바꾸기. 지금은 1위를 보여 준다.
  const rankIndex = 0;
  const ranking = ranked[rankIndex];

  return (
    <AdminPage
      title="호불호 평점 콘텐츠 관리"
      current="호불호 평점 콘텐츠 관리"
      description="평점 대상 콘텐츠와 호불호·리뷰 현황을 관리합니다."
    >
      <AdminHeading
        action={
          // TODO: 콘텐츠 등록 화면 연결
          <button
            type="button"
            style={{
              width: 138,
              height: 36,
              padding: "0 12px",
              border: "1px solid #999",
              background: "#fff",
              fontSize: 12,
              fontWeight: 700,
              textAlign: "left",
              cursor: "pointer",
            }}
          >
            콘텐츠 등록
          </button>
        }
      >
        호불호 평점 콘텐츠 현황
      </AdminHeading>
      <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
        <section
          aria-label="참여도 순위별 콘텐츠"
          style={{
            display: "flex",
            alignItems: "center",
            gap: 10,
            marginTop: -10,
            padding: "14px 16px",
            border: "1px solid #999",
          }}
        >
          <RankButton disabled={rankIndex === 0}>‹ 이전 순위</RankButton>
          <div style={{ flex: 1, minWidth: 0, padding: "0 10px" }}>
            <p style={{ fontSize: 12, fontWeight: 700 }}>
              참여도 {rankIndex + 1}위 / 상위 {RANKING_LIMIT}개
            </p>
            <h3 style={{ marginTop: 2, paddingLeft: 5, fontSize: 22, fontWeight: 700 }}>
              {ranking.title}
            </h3>
            <p style={{ marginTop: 4, fontSize: 12, lineHeight: 1.4, color: "#555" }}>
              참여자 {ranking.participantCount}명 = 좋아요 {ranking.likeCount}명 +
              싫어요 {ranking.dislikeCount}명
              <br />
              (전체 콘텐츠 · 참여자 수 내림차순)
            </p>
          </div>
          <RankButton disabled={rankIndex === ranked.length - 1}>다음 순위 ›</RankButton>
        </section>
        <div
          style={{
            display: "grid",
            gridTemplateColumns: "repeat(auto-fit, minmax(260px, 1fr))",
            gap: 24,
          }}
        >
          <TraitPanel
            title="좋아요한 사람 · 특성 TOP5"
            basis={`${ranking.title}에 좋아요를 누른 회원 프로필 기준`}
            traits={TOP_TRAITS.like}
          />
          <TraitPanel
            title="싫어요한 사람 · 특성 TOP5"
            basis={`${ranking.title}에 싫어요를 누른 회원 프로필 기준`}
            traits={TOP_TRAITS.dislike}
          />
        </div>
        <AdminFilters label="분류" filters={["전체", ...CATEGORIES]} />
        <AdminSearch placeholder="콘텐츠 제목 또는 분류 검색" />
        <AdminTable
          columns={[
            { label: "콘텐츠", width: 21 },
            { label: "분류", width: 14 },
            { label: "좋아요", width: 10 },
            { label: "싫어요", width: 10 },
            { label: "리뷰", width: 10 },
            { label: "상태", width: 16 },
          ]}
          rows={CONTENTS.map((content) => ({
            id: content.id,
            cells: [
              content.title,
              content.category,
              content.likeCount,
              content.dislikeCount,
              content.reviewCount,
              content.status,
            ],
          }))}
        />
        {/* TODO: 실제 현재 페이지와 전체 페이지 수 연결 */}
        <Pagination currentPage={1} totalPages={10} />
        <AdminDetail
          title={`선택한 콘텐츠 · ${selected.title}`}
          rows={[
            { label: "분류", value: selected.category.replace("/", " / ") },
            {
              label: "호불호",
              value: `좋아요 ${selected.likeCount} · 싫어요 ${selected.dislikeCount} · 리뷰 ${selected.reviewCount}개`,
            },
            { label: "현재 상태", value: selected.status },
          ]}
          actions={["상세 보기", "콘텐츠 편집", "숨김"]}
        />
      </div>
      <AdminNote>
        참여자 수(좋아요+싫어요) 상위 {RANKING_LIMIT}개 콘텐츠를 순위별로 탐색 · 각
        콘텐츠의 참여자 특성 TOP5 · 값은 예시
      </AdminNote>
    </AdminPage>
  );
}

function RankButton({
  disabled,
  children,
}: {
  disabled: boolean;
  children: string;
}) {
  return (
    // TODO: 누르면 이전·다음 순위 콘텐츠로 바꾸기
    <button
      type="button"
      disabled={disabled}
      style={{
        flex: "0 0 132px",
        height: 42,
        padding: "0 11px",
        border: "1px solid #999",
        background: "#fff",
        color: disabled ? "#aaa" : "#222",
        fontSize: 12,
        fontWeight: 700,
        textAlign: "left",
        cursor: disabled ? "default" : "pointer",
      }}
    >
      {children}
    </button>
  );
}

function TraitPanel({
  title,
  basis,
  traits,
}: {
  title: string;
  // 어떤 회원들의 프로필을 기준으로 했는지
  basis: string;
  traits: string[];
}) {
  return (
    <section style={{ padding: "10px 12px 18px", border: "1px solid #999" }}>
      <h3 style={{ fontSize: 14, fontWeight: 700 }}>{title}</h3>
      <p style={{ marginTop: 10, fontSize: 10, color: "#777" }}>
        {basis}
      </p>
      <ol
        style={{
          display: "flex",
          flexDirection: "column",
          gap: 3,
          marginTop: 16,
          listStyle: "none",
          fontSize: 11,
        }}
      >
        {traits.map((trait, index) => (
          <li
            key={trait}
            style={{
              display: "flex",
              alignItems: "center",
              gap: 16,
              height: 28,
              padding: "0 10px",
              border: "1px solid #999",
              // 홀수 순위만 회색으로 칠해 줄을 구분한다.
              background: index % 2 === 0 ? "#f4f4f4" : "#fff",
            }}
          >
            <span style={{ width: 10, fontWeight: 700 }}>{index + 1}</span>
            <span style={{ flex: 1 }}>{trait}</span>
            {/* TODO: 실제 인원 수 연결 */}
            <span style={{ color: "#777" }}>—명</span>
          </li>
        ))}
      </ol>
    </section>
  );
}
