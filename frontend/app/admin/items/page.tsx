import AdminPage from "@/app/admin/_components/AdminPage";
import Pagination from "@/components/Pagination";
import {
  AdminDetail,
  AdminFilters,
  AdminHeading,
  AdminNote,
  AdminSearch,
  AdminStats,
  AdminTable,
} from "@/app/admin/_components/AdminParts";

// 와이어프레임: 관리자 · 트렌드 아이템 관리 (admin/items)

const STATS = [
  { label: "전체 게시글", value: "—" },
  { label: "신고 접수", value: "—" },
  { label: "숨김", value: "—" },
];

// 트렌드 아이템의 대분류
const CATEGORIES = ["패션", "뷰티", "리빙", "취미", "디지털", "재테크"];

const POSTS = [
  {
    id: 1,
    title: "양말 추천해주세요ㅠㅠ",
    category: "패션",
    author: "예시회원 A",
    reportCount: 2,
    status: "게시 중",
    summary: "스타일에 맞는 양말 추천을 요청하는 게시글",
    recommendedCount: 3,
    commentCount: 2,
  },
  {
    id: 2,
    title: "화이트 스니커즈",
    category: "패션",
    author: "예시회원 B",
    reportCount: 0,
    status: "게시 중",
    summary: "흰색 스니커즈 추천을 요청하는 게시글",
    recommendedCount: 1,
    commentCount: 4,
  },
  {
    id: 3,
    title: "가을 데일리 룩",
    category: "패션",
    author: "예시회원 C",
    reportCount: 1,
    status: "숨김",
    summary: "가을 데일리 룩 추천을 요청하는 게시글",
    recommendedCount: 0,
    commentCount: 1,
  },
];

export default function AdminItemsPage() {
  const [selected] = POSTS;

  return (
    <AdminPage
      title="트렌드 아이템 관리"
      current="트렌드 아이템 관리"
      description="아이템 게시글과 신고 내역을 확인하고 노출 상태를 관리합니다."
    >
      <AdminHeading>트렌드 아이템 게시글</AdminHeading>
      <div style={{ display: "flex", flexDirection: "column", gap: 18 }}>
        <AdminStats stats={STATS} />
        <AdminFilters label="카테고리" filters={["전체", ...CATEGORIES]} />
        <AdminSearch placeholder="게시글 제목 또는 작성자 검색" />
        <AdminTable
          columns={[
            { label: "게시글", width: 29 },
            { label: "카테고리", width: 15 },
            { label: "작성자", width: 15 },
            { label: "신고", width: 10 },
            { label: "상태", width: 18 },
          ]}
          rows={POSTS.map((post) => ({
            id: post.id,
            cells: [post.title, post.category, post.author, post.reportCount, post.status],
          }))}
        />
        {/* TODO: 실제 현재 페이지와 전체 페이지 수 연결 */}
        <Pagination currentPage={1} totalPages={10} />
        <AdminDetail
          title={`선택한 게시글 · ${selected.title}`}
          rows={[
            { label: "본문", value: selected.summary },
            {
              value: `추천 아이템 ${selected.recommendedCount}개 · 댓글 ${selected.commentCount}개 · 신고 ${selected.reportCount}건`,
            },
            { label: "현재 상태", value: selected.status },
          ]}
          actions={["상세 보기", "숨김", "삭제"]}
        />
      </div>
      <AdminNote>카테고리·신고·노출 상태를 기준으로 게시글 관리 · 행과 수치는 예시</AdminNote>
    </AdminPage>
  );
}
