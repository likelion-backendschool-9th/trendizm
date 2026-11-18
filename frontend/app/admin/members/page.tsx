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

// 와이어프레임: 관리자 · 회원 관리 (admin/members)
// 비밀번호와 민감 정보는 목록에 표시하지 않는다.

const STATS = [
  { label: "전체 회원", value: "—" },
  { label: "활성 회원", value: "—" },
  { label: "이용 제한", value: "—" },
];

const STATUSES = ["활성", "이용 제한"];

const MEMBERS = [
  {
    id: 1,
    nickname: "예시회원 A",
    email: "member01@example.com",
    joinedAt: "YYYY.MM.DD",
    activityCount: 12,
    status: "활성",
    activity: { posts: 3, comments: 9, ratings: 2 },
  },
  {
    id: 2,
    nickname: "예시회원 B",
    email: "member02@example.com",
    joinedAt: "YYYY.MM.DD",
    activityCount: 4,
    status: "활성",
    activity: { posts: 1, comments: 2, ratings: 1 },
  },
  {
    id: 3,
    nickname: "예시회원 C",
    email: "member03@example.com",
    joinedAt: "YYYY.MM.DD",
    activityCount: 0,
    status: "이용 제한",
    activity: { posts: 0, comments: 0, ratings: 0 },
  },
];

export default function AdminMembersPage() {
  const [selected] = MEMBERS;

  return (
    <AdminPage
      title="회원 관리"
      current="회원 관리"
      description="회원 계정과 활동 상태를 조회하고 이용 상태를 관리합니다."
    >
      <AdminHeading>회원 현황</AdminHeading>
      <div style={{ display: "flex", flexDirection: "column", gap: 18 }}>
        <AdminStats stats={STATS} />
        <AdminFilters label="회원 상태" filters={["전체", ...STATUSES]} />
        <AdminSearch placeholder="닉네임 또는 이메일 검색" />
        <AdminTable
          columns={[
            { label: "닉네임", width: 17 },
            { label: "이메일", width: 29 },
            { label: "가입일", width: 16 },
            { label: "활동", width: 9 },
            { label: "상태", width: 15 },
          ]}
          rows={MEMBERS.map((member) => ({
            id: member.id,
            cells: [
              member.nickname,
              member.email,
              member.joinedAt,
              member.activityCount,
              member.status,
            ],
          }))}
        />
        {/* TODO: 실제 현재 페이지와 전체 페이지 수 연결 */}
        <Pagination currentPage={1} totalPages={10} />
        <AdminDetail
          title={`선택한 회원 · ${selected.nickname}`}
          rows={[
            { label: "이메일", value: selected.email },
            {
              label: "활동",
              value: `작성 글 ${selected.activity.posts}개 · 댓글 ${selected.activity.comments}개 · 평점 ${selected.activity.ratings}개`,
            },
          ]}
          actions={["활동 보기", "상태 저장"]}
        >
          <label
            style={{
              display: "flex",
              alignItems: "center",
              gap: 40,
              marginTop: 14,
              fontSize: 12,
              fontWeight: 700,
            }}
          >
            계정 상태
            <select
              name="status"
              defaultValue={selected.status}
              style={{
                width: 174,
                height: 32,
                padding: "0 8px",
                border: "1px solid #999",
                background: "#f4f4f4",
                fontSize: 12,
              }}
            >
              {STATUSES.map((status) => (
                <option key={status} value={status}>
                  {status}
                </option>
              ))}
            </select>
          </label>
        </AdminDetail>
      </div>
      <AdminNote>비밀번호와 민감 정보는 목록에 표시하지 않음 · 행과 수치는 예시</AdminNote>
    </AdminPage>
  );
}
