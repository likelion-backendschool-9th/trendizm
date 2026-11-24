type RatingImageProps = {
  src: string | null;
  alt: string;
  width: number;
  height: number;
};

// 콘텐츠 포스터. 관리자가 등록한 외부 이미지 주소라 next/image의 허용 도메인 설정을 쓸 수 없어서 <img>를 쓴다.
// 이미지가 없으면 회색 칸을 보여 준다.
export default function RatingImage({ src, alt, width, height }: RatingImageProps) {
  if (!src) {
    return (
      <div
        aria-hidden
        style={{ flex: `0 0 ${width}px`, height, borderRadius: 6, background: "#ddd" }}
      />
    );
  }
  return (
    // eslint-disable-next-line @next/next/no-img-element
    <img
      src={src}
      alt={alt}
      width={width}
      height={height}
      style={{
        flex: `0 0 ${width}px`,
        width,
        height,
        borderRadius: 6,
        objectFit: "cover",
        border: "1px solid #ddd",
      }}
    />
  );
}
