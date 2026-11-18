import Link from "next/link";
import AdminPage from "@/app/admin/_components/AdminPage";
import { AdminHeading, AdminStats } from "@/app/admin/_components/AdminParts";

// 와이어프레임: 관리자 · 대시보드 (admin)

const STATS = [
  { label: "전체 회원", value: "—" },
  { label: "등록된 밈", value: "—" },
  { label: "트렌드 아이템", value: "—" },
  { label: "평점 콘텐츠", value: "—" },
  { label: "피드 영상", value: "—" },
];

// 관리 항목별 최근 목록. 영역마다 최대 5개.
const SECTIONS = [
  {
    title: "밈 등록 관리",
    href: "/admin/memes",
    rows: [
      { id: 1, name: "알잘딱깔센", meta: "의견 3개", date: "YYYY-MM-DD" },
      { id: 2, name: "폼 미쳤다", meta: "의견 5개", date: "YYYY-MM-DD" },
      { id: 3, name: "억까", meta: "의견 2개", date: "YYYY-MM-DD" },
      { id: 4, name: "예시 밈 D", meta: "의견 —", date: "YYYY-MM-DD" },
      { id: 5, name: "예시 밈 E", meta: "의견 —", date: "YYYY-MM-DD" },
    ],
  },
  {
    title: "트렌드 아이템",
    href: "/admin/items",
    rows: [
      { id: 1, name: "양말 추천해주세요ㅠㅠ", meta: "패션", date: "YYYY-MM-DD" },
      { id: 2, name: "화이트 스니커즈", meta: "패션", date: "YYYY-MM-DD" },
      { id: 3, name: "가을 데일리 룩", meta: "패션", date: "YYYY-MM-DD" },
      { id: 4, name: "예시 게시글 D", meta: "뷰티", date: "YYYY-MM-DD" },
      { id: 5, name: "예시 게시글 E", meta: "리빙", date: "YYYY-MM-DD" },
    ],
  },
  {
    title: "호불호 평점 콘텐츠",
    href: "/admin/ratings",
    rows: [
      { id: 1, name: "예시 영화 A", meta: "미디어/영화", date: "YYYY-MM-DD" },
      { id: 2, name: "예시 음료 B", meta: "푸드/음료", date: "YYYY-MM-DD" },
      { id: 3, name: "예시 게임 C", meta: "게임", date: "YYYY-MM-DD" },
      { id: 4, name: "예시 콘텐츠 D", meta: "음악", date: "YYYY-MM-DD" },
      { id: 5, name: "예시 콘텐츠 E", meta: "도서·웹툰", date: "YYYY-MM-DD" },
    ],
  },
  {
    title: "피드 리스트",
    href: "/admin/feeds",
    rows: [
      { id: 1, name: "생성형 AI, 일상에서", meta: "YouTube", date: "YYYY-MM-DD" },
      { id: 2, name: "AI 도구로 작업 시간", meta: "Vimeo", date: "YYYY-MM-DD" },
      { id: 3, name: "ChatGPT 질문 팁", meta: "YouTube", date: "YYYY-MM-DD" },
      { id: 4, name: "오늘의 AI 소식", meta: "Dailymotion", date: "YYYY-MM-DD" },
      { id: 5, name: "예시 영상 E", meta: "YouTube", date: "YYYY-MM-DD" },
    ],
  },
];

export default function AdminDashboardPage() {
  return (
    <AdminPage
      title="관리자 대시보드"
      description="서비스 현황을 확인하고 각 관리 메뉴로 이동합니다."
    >
      <AdminHeading>운영 요약</AdminHeading>
      <p style={{ marginTop: -12, fontSize: 12, color: "#555" }}>
        주요 서비스 지표와 관리 영역을 한곳에서 확인합니다.
      </p>

      <h3 style={{ marginTop: 22, marginBottom: 12, fontSize: 16, fontWeight: 700 }}>
        주요 지표
      </h3>
      <AdminStats stats={STATS} />

      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "baseline",
          marginTop: 20,
        }}
      >
        <h3 style={{ fontSize: 16, fontWeight: 700 }}>관리 항목별 목록</h3>
        <p style={{ fontSize: 11, color: "#555" }}>예시 목록 · 최대 5개</p>
      </div>
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(300px, 1fr))",
          gap: "12px 24px",
          marginTop: 12,
          marginBottom: 6,
        }}
      >
        {SECTIONS.map((section) => (
          <section
            key={section.href}
            style={{ padding: "12px 14px 24px", border: "1px solid #999" }}
          >
            <header
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                paddingBottom: 10,
                borderBottom: "1px solid #999",
              }}
            >
              <h4 style={{ fontSize: 14, fontWeight: 700 }}>{section.title}</h4>
              <Link
                href={section.href}
                style={{ paddingRight: 16, fontSize: 12, fontWeight: 700 }}
              >
                전체 보기 →
              </Link>
            </header>
            <ul style={{ marginTop: 8, listStyle: "none", fontSize: 11 }}>
              {section.rows.map((row, index) => (
                <li
                  key={row.id}
                  style={{
                    display: "flex",
                    alignItems: "center",
                    gap: 8,
                    height: 29,
                    padding: "0 6px",
                    // 홀수 줄만 회색으로 칠해 줄을 구분한다.
                    background: index % 2 === 0 ? "#f4f4f4" : "#fff",
                  }}
                >
                  <span
                    style={{
                      flex: 1,
                      minWidth: 0,
                      overflow: "hidden",
                      textOverflow: "ellipsis",
                      whiteSpace: "nowrap",
                    }}
                  >
                    {row.name}
                  </span>
                  <span style={{ width: 76, color: "#777" }}>{row.meta}</span>
                  <span style={{ color: "#777" }}>{row.date}</span>
                </li>
              ))}
            </ul>
          </section>
        ))}
      </div>
    </AdminPage>
  );
}
