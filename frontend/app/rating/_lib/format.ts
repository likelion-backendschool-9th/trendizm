// 호불호 평점 화면에서 쓰는 표시 형식.

/** 2026-11-24 또는 2026-11-24T10:15:30 → 2026.11.24 */
export function formatDate(value: string): string {
  const [date] = value.split("T");
  return date.replaceAll("-", ".");
}

/** 추가 정보(extra)를 화면 문구로. 예: { season: 2, episodeLength: "60분" } → ["시즌 2", "에피소드당 60분"] */
export function extraLabels(extra: Record<string, unknown> | null): string[] {
  if (!extra) {
    return [];
  }
  const hasValue = (value: unknown) => value !== undefined && value !== null && value !== "";
  const labels: string[] = [];
  if (hasValue(extra.season)) {
    labels.push(`시즌 ${extra.season}`);
  }
  if (hasValue(extra.episodeLength)) {
    labels.push(`에피소드당 ${extra.episodeLength}`);
  }
  return labels;
}

/** 평균 별점. 후기가 없으면 "-" */
export function formatScore(score: number | null): string {
  return score === null ? "-" : score.toFixed(1);
}
