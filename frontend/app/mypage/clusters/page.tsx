// 와이어프레임: 클러스터 설정

const QUERY = "AI";

const SEARCH_RESULTS = [
  "AI 음성 도용 제보",
  "AI 영상 삭제 논란",
  "AI 면접 점수 공개",
  "AI 자막 오역 캡처",
  "AI 필터 원본 유출",
  "AI 광고 표절 논란",
  "AI 사진 공모전 수상",
  "AI 캐릭터 무단 학습",
  "AI 보컬 커버 삭제",
  "AI 강의 과제 논쟁",
  "AI 합성 인터뷰 확산",
  "AI 여행 일정 오류",
  "AI 레시피 따라하기",
  "AI 게임 NPC 대화",
  "AI 웹툰 작화 논란",
  "AI 라이브 더빙 사고",
  "AI 쇼츠 요약 오류",
  "AI 회의록 유출",
  "AI 보정 전후 사진",
  "AI 작곡 음원 차트",
  "AI 이력서 합격 후기",
  "AI 코드 리뷰 논쟁",
  "AI 수학 풀이 오답",
  "AI 책 표지 표절",
  "AI 아바타 챌린지",
];

const CLUSTERS = [
  "지브리풍 사진",
  "AI 성우 사칭",
  "무료 영상 생성",
  "챗봇 오답 캡처",
  "프롬프트 유출",
  "AI 면접 후기",
  "자동 자막 오역",
  "신형 모델 공개",
  "AI 그림 저작권",
  "AI 광고 패러디",
  "로봇 바리스타",
  "컴백 티저 날짜",
  "앵콜 라이브 논란",
  "팬미팅 추가 좌석",
  "직캠 100만 조회",
  "최종화 반전 장면",
  "쿠키 영상 해석",
  "웹툰 캐스팅 발표",
  "신작 서버 점검",
  "결승 역전 골",
  "9회말 끝내기",
  "러닝화 재입고",
  "성수 팝업 대기",
  "편의점 신상빵",
  "카페 한정 메뉴",
  "1분 계란찜",
  "제주 항공권 특가",
  "캠핑장 예약 전쟁",
  "반려견 동반 카페",
  "올영 품절템",
  "주가 급등 이유",
  "한강 러닝 인증",
  "전시 굿즈 완판",
];

export default function ClustersPage() {
  return (
    <main style={{ display: "flex", flexDirection: "column", gap: 40 }}>
      <section>
        <h1 style={{ fontSize: 24, fontWeight: 700 }}>클러스터 검색</h1>
        {/* TODO: 클러스터 검색 요청 연결 */}
        <form style={{ display: "flex", gap: 12, marginTop: 12 }}>
          <input
            type="search"
            name="query"
            placeholder="검색어"
            defaultValue={QUERY}
            style={{
              flex: "0 1 360px",
              minWidth: 0,
              height: 30,
              padding: "0 10px",
              border: "1px solid #111",
              fontSize: 15,
            }}
          />
          <button
            type="submit"
            style={{
              padding: "0 12px",
              border: "1px solid #111",
              borderRadius: 999,
              background: "#fff",
              fontSize: 15,
              fontWeight: 700,
              cursor: "pointer",
            }}
          >
            검색
          </button>
        </form>

        <div style={{ marginTop: 14, padding: "10px 14px 14px", border: "1px solid #111" }}>
          <p style={{ fontSize: 13, fontWeight: 700 }}>
            검색 결과 {SEARCH_RESULTS.length}개
          </p>
          <ul
            style={{
              display: "flex",
              flexWrap: "wrap",
              gap: 6,
              marginTop: 12,
              listStyle: "none",
            }}
          >
            {SEARCH_RESULTS.map((cluster) => (
              <li
                key={cluster}
                style={{
                  padding: "4px 20px",
                  border: "1px solid #111",
                  borderRadius: 999,
                  fontSize: 14,
                  fontWeight: 700,
                }}
              >
                {cluster}
              </li>
            ))}
          </ul>
          <p style={{ marginTop: 10, fontSize: 11, textAlign: "center" }}>
            클러스터 아이템을 아래 설정으로 끌어 추가
          </p>
        </div>
      </section>

      <section>
        <h2 style={{ fontSize: 24, fontWeight: 700 }}>클러스터 설정</h2>
        {/* TODO: 추가/삭제/적용 연결 */}
        <div style={{ display: "flex", gap: 6, marginTop: 12 }}>
          <input
            type="text"
            name="cluster"
            aria-label="추가할 클러스터"
            style={{
              width: 140,
              height: 30,
              marginRight: 6,
              padding: "0 10px",
              border: "1px solid #111",
              fontSize: 14,
            }}
          />
          {["추가", "삭제", "적용"].map((action) => (
            <button
              key={action}
              type="button"
              style={{
                padding: "0 12px",
                border: "1px solid #111",
                borderRadius: 999,
                background: "#fff",
                fontSize: 15,
                fontWeight: 700,
                cursor: "pointer",
              }}
            >
              {action}
            </button>
          ))}
        </div>

        <div style={{ marginTop: 18, padding: "10px 14px 20px", border: "1px solid #111" }}>
          <p style={{ fontSize: 13, fontWeight: 700 }}>
            설정된 클러스터 {CLUSTERS.length}개
          </p>
          {/* TODO: 클러스터 하나를 박스 밖으로 끌어 놓으면 삭제 */}
          <ul
            style={{
              display: "flex",
              flexWrap: "wrap",
              gap: 6,
              marginTop: 12,
              listStyle: "none",
            }}
          >
            {CLUSTERS.map((cluster) => (
              <li
                key={cluster}
                style={{
                  padding: "4px 14px",
                  border: "1px solid #111",
                  borderRadius: 999,
                  fontSize: 14,
                  fontWeight: 700,
                }}
              >
                {cluster}
              </li>
            ))}
          </ul>

          {/* TODO: 검색 결과가 1개 이상일 때만 표시. 끌어 놓으면 설정에 추가 */}
          <div
            style={{
              marginTop: 20,
              padding: "14px 0",
              border: "1px solid #999",
              background: "#f2f2f2",
              color: "#555",
              textAlign: "center",
              fontSize: 14,
              fontWeight: 700,
            }}
          >
            + 검색 결과를 여기에 끌어 추가
          </div>
        </div>
      </section>
    </main>
  );
}
