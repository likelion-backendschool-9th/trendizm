// 트렌드 아이템 화면에서 쓰는 표시 형식.

/** 백엔드 시각(예: 2026-11-24T10:15:30.123456)을 2026.11.24로 */
export function formatDate(value: string): string {
  const [date] = value.split("T");
  return date.replaceAll("-", ".");
}

/** 10000 → 10,000원 */
export function formatPrice(value: number): string {
  return `${value.toLocaleString("ko-KR")}원`;
}

/** 추천이 하나라도 있으면 추천 수, 없으면 답변 대기중 */
export function answerLabel(recommendationCount: number): string {
  return recommendationCount > 0 ? `추천 ${recommendationCount}개` : "답변 대기중";
}
