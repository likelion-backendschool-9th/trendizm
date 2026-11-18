import VideoFeed, { type FeedVideo } from "@/app/feed/_components/VideoFeed";

// 와이어프레임: 피드 (롱폼)

const KEYWORDS = [
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

const VIDEOS: FeedVideo[] = [
  {
    id: 1,
    title: "생성형 AI, 일상에서 쓰는 법",
    platform: "YouTube",
    duration: "08:24",
    meta: ["채널 A", "조회 12.4만", "2일 전"],
  },
  {
    id: 2,
    title: "AI 도구로 작업 시간 줄이기",
    platform: "Dailymotion",
    duration: "12:36",
    meta: ["채널 B", "조회 8.1만", "3일 전"],
  },
  {
    id: 3,
    title: "이번 주 AI 이슈 정리",
    platform: "Vimeo",
    duration: "17:08",
    meta: ["채널 C", "조회 5.6만", "5일 전"],
  },
  {
    id: 4,
    title: "새 모델 비교와 활용 사례",
    platform: "YouTube",
    duration: "21:14",
    meta: ["채널 D", "조회 3.2만", "1주 전"],
  },
  {
    id: 5,
    title: "AI가 바꾸는 콘텐츠 제작",
    platform: "Dailymotion",
    duration: "14:42",
    meta: ["채널 E", "조회 2.8만", "1주 전"],
  },
  {
    id: 6,
    title: "실무에서 써 본 AI 기능 5가지",
    platform: "Vimeo",
    duration: "11:05",
    meta: ["채널 F", "조회 1.9만", "2주 전"],
  },
  {
    id: 7,
    title: "AI 검색의 현재와 다음 단계",
    platform: "YouTube",
    duration: "18:20",
    meta: ["채널 G", "조회 1.4만", "2주 전"],
  },
  {
    id: 8,
    title: "생성형 AI 활용 사례 모음",
    platform: "Dailymotion",
    duration: "16:32",
    meta: ["채널 H", "조회 9천", "3주 전"],
  },
];

export default function FeedLongPage() {
  return <VideoFeed variant="long" keywords={KEYWORDS} videos={VIDEOS} />;
}
