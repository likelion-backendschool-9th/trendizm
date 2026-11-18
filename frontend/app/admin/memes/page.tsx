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

// 와이어프레임: 관리자 · 밈 등록 관리 (admin/memes)

const STATS = [
  { label: "전체 밈", value: "—" },
  { label: "공개 중", value: "—" },
  { label: "숨김", value: "—" },
];

const MEMES = [
  { id: 1, word: "알잘딱깔센", url: "example.com/post/001", opinionCount: 3, createdAt: "YYYY.MM.DD", status: "공개 중" },
  { id: 2, word: "폼 미쳤다", url: "example.com/post/002", opinionCount: 5, createdAt: "YYYY.MM.DD", status: "공개 중" },
  { id: 3, word: "억까", url: "example.com/post/003", opinionCount: 2, createdAt: "YYYY.MM.DD", status: "숨김" },
];

export default function AdminMemesPage() {
  const [selected] = MEMES;

  return (
    <AdminPage
      title="밈 등록 관리"
      current="밈 등록 관리"
      description="검색된 단어와 원문 링크, 등록된 의견을 확인하고 노출 상태를 관리합니다."
    >
      <AdminHeading>밈 등록 현황</AdminHeading>
      <div style={{ display: "flex", flexDirection: "column", gap: 18 }}>
        <AdminStats stats={STATS} />
        <AdminFilters label="노출 상태" filters={["전체", "공개 중", "숨김"]} />
        <AdminSearch placeholder="단어 또는 원문 URL 검색" />
        <AdminTable
          columns={[
            { label: "단어", width: 18 },
            { label: "원문 URL", width: 27 },
            { label: "의견", width: 10 },
            { label: "등록일", width: 17 },
            { label: "상태", width: 14 },
          ]}
          rows={MEMES.map((meme) => ({
            id: meme.id,
            cells: [meme.word, meme.url, meme.opinionCount, meme.createdAt, meme.status],
          }))}
        />
        {/* TODO: 실제 현재 페이지와 전체 페이지 수 연결 */}
        <Pagination currentPage={1} totalPages={10} />
        <AdminDetail
          title={`선택한 밈 · ${selected.word}`}
          rows={[
            { label: "원문 URL", value: selected.url },
            {
              label: "등록 의견",
              value: `${selected.opinionCount}개 · 뜻 요약과 사용 예시 확인`,
            },
            { label: "현재 상태", value: selected.status },
          ]}
          actions={["상세 보기", "수정", "숨김"]}
        />
      </div>
      <AdminNote>
        의견 1개 이상인 문서는 별도 승인 없이 등록 · 관리자는 노출 상태와 내용을 관리
      </AdminNote>
    </AdminPage>
  );
}
