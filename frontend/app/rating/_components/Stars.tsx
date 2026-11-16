type StarsProps = {
  score: number;
  // 채워진 별 색. 목록은 노란색, 상세는 검은색을 쓴다.
  color?: string;
  size?: number;
};

// 5점 만점 별점. 소수점은 반올림해 별 개수로 보여 준다.
export default function Stars({ score, color = "#f5c518", size = 12 }: StarsProps) {
  const filled = Math.round(score);

  return (
    <span
      role="img"
      aria-label={`별점 ${score.toFixed(1)}점`}
      style={{ fontSize: size, letterSpacing: 1, whiteSpace: "nowrap" }}
    >
      {[1, 2, 3, 4, 5].map((star) => (
        <span key={star} style={{ color: star <= filled ? color : "#ccc" }}>
          ★
        </span>
      ))}
    </span>
  );
}
