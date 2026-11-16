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

// 와이어프레임: 관리자 · 피드 리스트 관리 (admin/feeds)

const PLATFORMS = ["YouTube", "Dailymotion", "Vimeo"];

const VIDEOS = [
  {
    id: 1,
    title: "생성형 AI, 일상에서 쓰는 법",
    url: "https://example.com/video/001",
    platform: "YouTube",
    form: "롱폼",
    duration: "08:24",
    cluster: "지브리풍 사진",
    status: "노출",
  },
  {
    id: 2,
    title: "AI 도구로 작업 시간 줄이기",
    url: "https://example.com/video/002",
    platform: "Vimeo",
    form: "롱폼",
    duration: "17:08",
    cluster: "무료 영상 생성",
    status: "노출",
  },
  {
    id: 3,
    title: "ChatGPT 질문 팁",
    url: "https://example.com/video/003",
    platform: "YouTube",
    form: "숏폼",
    duration: "00:24",
    cluster: "챗봇 오답 캡처",
    status: "노출",
  },
  {
    id: 4,
    title: "오늘의 AI 소식",
    url: "https://example.com/video/004",
    platform: "Dailymotion",
    form: "숏폼",
    duration: "00:48",
    cluster: "신형 모델 공개",
    status: "숨김",
  },
];

export default function AdminFeedsPage() {
  const [selected] = VIDEOS;

  return (
    <AdminPage
      title="피드 리스트 관리"
      current="피드 리스트 관리"
      description="롱폼·숏폼 영상의 링크와 플랫폼, 목록 노출 상태를 관리합니다."
    >
      <AdminHeading>피드 영상 목록</AdminHeading>
      <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
        <AdminFilters
          label="영상 구분"
          filters={["전체 영상", "롱폼", "숏폼"]}
          maxWidth={425}
        />
        <section>
          <h3 style={{ marginBottom: 12, fontSize: 15, fontWeight: 700 }}>
            영상 URL 등록
          </h3>
          <AdminSearch
            type="url"
            name="url"
            placeholder="https://영상-원본-주소"
            button="영상 추가"
          />
        </section>
        <AdminFilters label="플랫폼" filters={["전체 플랫폼", ...PLATFORMS]} />
        <AdminSearch placeholder="영상 제목 또는 채널 검색" />
        <AdminTable
          columns={[
            { label: "영상 제목", width: 25 },
            { label: "플랫폼", width: 14 },
            { label: "구분", width: 9 },
            { label: "길이", width: 9 },
            { label: "노출 클러스터", width: 15 },
            { label: "상태", width: 11 },
          ]}
          rows={VIDEOS.map((video) => ({
            id: video.id,
            cells: [
              video.title,
              video.platform,
              video.form,
              video.duration,
              video.cluster,
              video.status,
            ],
          }))}
        />
        {/* TODO: 실제 현재 페이지와 전체 페이지 수 연결 */}
        <Pagination currentPage={1} totalPages={10} />
        <AdminDetail
          title={`선택한 영상 · ${selected.title}`}
          rows={[
            { label: "원본 URL", value: selected.url },
            {
              value: `${selected.platform} · ${selected.form} · ${selected.duration}`,
            },
            {
              value: (
                <strong>
                  노출 클러스터 {selected.cluster} → 이 클러스터를 선택한 회원의
                  피드에 표시
                </strong>
              ),
            },
          ]}
          actions={["원본 보기", "숨김", "목록 제외"]}
        />
      </div>
      <AdminNote>
        노출 클러스터가 회원의 설정 클러스터와 일치할 때 목록에 표시 · 행은 예시
      </AdminNote>
    </AdminPage>
  );
}
