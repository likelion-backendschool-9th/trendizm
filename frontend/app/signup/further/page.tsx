import Link from "next/link";
import ChoiceGroup from "@/app/signup/_components/ChoiceGroup";

// 와이어프레임: 회원가입 직후 추가정보 입력 (signup/further)
// 모든 항목은 선택 입력이다.

type Question = {
  name: string;
  title: string;
  multiple: boolean;
  options: string[];
};

type Section = {
  title: string;
  description: string;
  questions: Question[];
};

const SECTIONS: Section[] = [
  {
    title: "기본 성향",
    description: "나를 설명하는 기본 범주를 선택해 주세요.",
    questions: [
      {
        name: "ageGroup",
        title: "연령대",
        multiple: false,
        options: ["10대", "20대 초반", "20대 후반", "30대", "40대", "50대 이상", "선택 안 함"],
      },
      {
        name: "gender",
        title: "성별",
        multiple: false,
        options: ["여성", "남성", "논바이너리", "기타", "응답하지 않음"],
      },
      {
        name: "mbti",
        title: "MBTI",
        multiple: false,
        options: [
          "ISTJ",
          "ISFJ",
          "INFJ",
          "INTJ",
          "ISTP",
          "ISFP",
          "INFP",
          "INTP",
          "ESTP",
          "ESFP",
          "ENFP",
          "ENTP",
          "ESTJ",
          "ESFJ",
          "ENFJ",
          "ENTJ",
          "모름/선택 안 함",
        ],
      },
      {
        name: "lifeStage",
        title: "현재 생활 단계",
        multiple: false,
        options: [
          "학생",
          "취업 준비",
          "사회 초년생",
          "경력 직장인",
          "프리랜서",
          "자영업",
          "육아 중",
          "은퇴/휴식",
        ],
      },
      {
        name: "occupation",
        title: "직업·직군",
        multiple: true,
        options: [
          "개발/IT",
          "디자인",
          "콘텐츠/미디어",
          "교육",
          "의료/보건",
          "금융",
          "제조/기술",
          "서비스",
          "공공",
          "영업/마케팅",
          "기타",
        ],
      },
    ],
  },
  {
    title: "관심사와 취미",
    description: "좋아하는 주제와 평소 활동을 알려주세요.",
    questions: [
      {
        name: "interests",
        title: "관심 분야",
        multiple: true,
        options: [
          "밈/인터넷 문화",
          "음악",
          "게임",
          "영화/드라마",
          "웹툰",
          "패션",
          "뷰티",
          "음식",
          "여행",
          "운동",
          "반려동물",
          "인테리어",
          "테크",
          "경제/투자",
          "육아/가족",
          "책/지식",
        ],
      },
      {
        name: "hobbies",
        title: "취미·활동",
        multiple: true,
        options: [
          "독서",
          "요리",
          "사진/영상",
          "창작",
          "공연/전시",
          "운동/스포츠",
          "게임",
          "여행",
          "쇼핑",
          "수집",
          "캠핑",
          "커뮤니티 활동",
          "반려동물 돌봄",
          "DIY",
        ],
      },
      {
        name: "activitySpace",
        title: "선호 활동 공간",
        multiple: false,
        options: ["실내 위주", "실외 위주", "둘 다"],
      },
      {
        name: "activityStyle",
        title: "활동 방식",
        multiple: false,
        options: ["혼자 하는 편", "함께 하는 편", "상황에 따라"],
      },
    ],
  },
  {
    title: "콘텐츠 이용 방식",
    description: "어디에서, 어떤 형식으로 트렌드를 접하는지 선택해 주세요.",
    questions: [
      {
        name: "platforms",
        title: "자주 쓰는 플랫폼",
        multiple: true,
        options: [
          "YouTube",
          "Instagram",
          "TikTok",
          "Threads",
          "X",
          "네이버 클립",
          "OTT",
          "온라인 커뮤니티",
          "기타",
        ],
      },
      {
        name: "formats",
        title: "선호 콘텐츠 형식",
        multiple: true,
        options: ["숏폼", "롱폼", "이미지", "텍스트", "라이브 방송", "팟캐스트"],
      },
      {
        name: "frequency",
        title: "콘텐츠 이용 빈도",
        multiple: false,
        options: ["하루 여러 번", "매일", "주 3~5일", "주 1~2일", "가끔"],
      },
      {
        name: "timeOfDay",
        title: "주로 보는 시간대",
        multiple: true,
        options: ["아침", "점심/휴식", "이동 중", "저녁", "늦은 밤"],
      },
      {
        name: "discovery",
        title: "새 콘텐츠를 찾는 경로",
        multiple: true,
        options: [
          "추천 피드",
          "직접 검색",
          "친구/지인",
          "커뮤니티",
          "뉴스/매체",
          "크리에이터 구독",
        ],
      },
    ],
  },
  {
    title: "콘텐츠 취향",
    description: "평소 즐겨 보는 콘텐츠의 형식과 주제, 취향을 알려주세요.",
    questions: [
      {
        name: "favoriteFormats",
        title: "평소 즐겨 보는 콘텐츠 형식",
        multiple: true,
        options: ["숏폼 영상", "긴 영상", "이미지·짤", "글·웹툰"],
      },
      {
        name: "priority",
        title: "콘텐츠를 고를 때 가장 중요한 점",
        multiple: false,
        options: ["재미", "유익한 정보", "공감", "감성·분위기"],
      },
      {
        name: "topics",
        title: "관심 있는 콘텐츠 주제",
        multiple: true,
        options: ["음악·공연", "영화·드라마", "패션·뷰티", "게임", "음식·여행", "일상"],
      },
      {
        name: "moods",
        title: "좋아하는 콘텐츠 분위기",
        multiple: true,
        options: ["유쾌함", "편안함", "감성", "몰입감", "유익함", "공감", "반전"],
      },
      {
        name: "viewingStyle",
        title: "콘텐츠를 즐기는 방식",
        multiple: true,
        options: ["가볍게 보기", "정주행", "저장해두기", "댓글 보기", "친구와 공유"],
      },
    ],
  },
  {
    title: "평가와 추천 취향",
    description: "호·불호 평가와 트렌드 아이템 추천에 참고할 선택입니다.",
    questions: [
      {
        name: "genres",
        title: "즐겨 보는 콘텐츠 장르",
        multiple: true,
        options: [
          "예능",
          "드라마",
          "영화",
          "애니메이션",
          "스포츠",
          "게임",
          "교육/정보",
          "음악",
          "웹툰",
        ],
      },
      {
        name: "ratingCriteria",
        title: "호·불호 판단 기준",
        multiple: true,
        options: ["재미", "공감", "완성도", "새로움", "실용성", "가성비", "대중성"],
      },
      {
        name: "productCategories",
        title: "관심 제품 분야",
        multiple: true,
        options: ["패션", "뷰티", "인테리어", "전자기기", "식품", "여행 상품", "취미 용품"],
      },
      {
        name: "selectionCriteria",
        title: "상품·콘텐츠 선택 기준",
        multiple: true,
        options: [
          "후기/평점",
          "가격",
          "브랜드",
          "유행성",
          "품질",
          "친구 추천",
          "내 취향과의 적합성",
        ],
      },
      {
        name: "recommendationRange",
        title: "추천 범위",
        multiple: false,
        options: ["익숙한 취향 위주", "새로운 발견 위주", "균형 있게"],
      },
      {
        name: "avoid",
        title: "피하고 싶은 콘텐츠 경험",
        multiple: true,
        options: [
          "스포일러",
          "광고성 콘텐츠",
          "과도한 반복",
          "너무 긴 영상",
          "자극적인 표현",
          "없음",
        ],
      },
    ],
  },
];

export default function SignupFurtherPage() {
  return (
    <main
      style={{
        width: "100%",
        maxWidth: 800,
        margin: "0 auto",
        padding: "44px 80px 60px",
        background: "#fff",
        color: "#222",
      }}
    >
      <h1 style={{ marginTop: 18, fontSize: 30, fontWeight: 700 }}>
        나에게 맞는 트렌드를 설정해요
      </h1>
      <p style={{ marginTop: 20, fontSize: 13, lineHeight: 1.5, color: "#555" }}>
        관심사와 콘텐츠 이용 방식을 알려주시면 탐색 경험을 조정할 수 있어요.
        <br />
        모든 항목은 선택 입력이며, 나중에 변경할 수 있습니다.
      </p>

      <ol
        aria-label="가입 단계"
        style={{
          display: "flex",
          alignItems: "center",
          gap: 12,
          marginTop: 44,
          listStyle: "none",
          fontSize: 12,
          color: "#555",
        }}
      >
        <li>계정 생성 완료</li>
        <li aria-hidden style={{ width: 100, borderTop: "1px solid #555" }} />
        <li aria-current="step" style={{ color: "#222" }}>
          ② 추가 정보 입력
        </li>
      </ol>
      <p style={{ marginTop: 20, fontSize: 11, color: "#777" }}>
        정확한 생년월일·회사명·연락처 대신 구간과 관심 분야를 선택합니다.
      </p>

      {/* TODO: 취향 저장 요청 연결 후 첫 화면으로 이동 */}
      <form style={{ marginTop: 28 }}>
        <div style={{ display: "flex", flexDirection: "column", gap: 24 }}>
          {SECTIONS.map((section, index) => (
            <section
              key={section.title}
              style={{ padding: 24, border: "1px solid #ccc" }}
            >
              <h2 style={{ fontSize: 18, fontWeight: 700 }}>
                {String(index + 1).padStart(2, "0")} · {section.title}
              </h2>
              <p style={{ marginTop: 18, fontSize: 12, color: "#777" }}>
                {section.description}
              </p>
              <div
                style={{
                  display: "flex",
                  flexDirection: "column",
                  gap: 20,
                  marginTop: 20,
                }}
              >
                {section.questions.map((question) => (
                  <ChoiceGroup
                    key={question.name}
                    name={question.name}
                    legend={question.title}
                    options={question.options}
                    multiple={question.multiple}
                  />
                ))}
              </div>
            </section>
          ))}
        </div>

        <button
          type="submit"
          style={{
            width: "100%",
            height: 52,
            marginTop: 34,
            border: "none",
            background: "#222",
            color: "#fff",
            fontSize: 15,
            fontWeight: 700,
            cursor: "pointer",
          }}
        >
          취향 저장하고 시작하기
        </button>
      </form>

      {/* 저장하지 않고 첫 화면으로 간다. */}
      <Link
        href="/"
        style={{
          display: "block",
          marginTop: 16,
          textAlign: "center",
          fontSize: 15,
        }}
      >
        지금은 건너뛰기
      </Link>
      <p style={{ marginTop: 16, fontSize: 12, color: "#777", textAlign: "center" }}>
        선택한 정보는 나중에 다시 확인하고 변경할 수 있습니다.
      </p>
    </main>
  );
}
