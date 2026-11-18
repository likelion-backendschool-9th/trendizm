import Image from "next/image";
import Link from "next/link";

export type SavedContent =
  | {
      type: "trend";
      id: number;
      category: string;
      title: string;
      summary: string;
      commentCount: number;
      // 추천받은 상품 중 등록된 순으로 첫 번째. image는 public 폴더 기준 경로
      recommendedItem: { image?: string; name: string; price: string };
    }
  | {
      type: "rating";
      id: number;
      title: string;
      meta: string;
      likeRatio: number;
      dislikeRatio: number;
    };

// 저장한 콘텐츠 카드. 마이페이지와 저장한 콘텐츠 화면에서 함께 쓴다.
export default function SavedContentCard({ content }: { content: SavedContent }) {
  if (content.type === "trend") {
    return (
      <article
        style={{
          display: "flex",
          flexWrap: "wrap",
          gap: 16,
          padding: "20px 16px 20px",
          border: "1px solid #999",
        }}
      >
        <div style={{ flex: "1 1 320px", minWidth: 0 }}>
          <p style={{ fontSize: 12, color: "#666" }}>트렌드 아이템</p>
          <h3
            style={{
              display: "flex",
              alignItems: "center",
              gap: 10,
              marginTop: 8,
              fontSize: 19,
              fontWeight: 700,
            }}
          >
            <span
              style={{
                padding: "2px 12px",
                border: "1px solid #111",
                fontSize: 13,
                fontWeight: 400,
              }}
            >
              {content.category}
            </span>
            <Link href={`/item/${content.id}`}>{content.title}</Link>
          </h3>
          <p style={{ marginTop: 12, fontSize: 13, lineHeight: 1.4 }}>
            {content.summary}
          </p>
          <p
            style={{
              display: "inline-block",
              marginTop: 12,
              padding: "3px 14px",
              background: "#e5e5e5",
              fontSize: 12,
            }}
          >
            댓글 {content.commentCount}개
          </p>
        </div>

        <div style={{ flex: "0 0 136px" }}>
          <p style={{ fontSize: 10, color: "#555", textAlign: "center" }}>
            추천받은 상품 1 (등록된 순)
          </p>
          {content.recommendedItem.image ? (
            // 테두리를 이미지에 직접 주면 그려지는 크기가 지정 크기와 달라져서 바깥 상자에 준다.
            <div
              style={{
                marginTop: 4,
                borderTop: "1px solid #eee",
                borderBottom: "1px solid #eee",
              }}
            >
              <Image
                src={content.recommendedItem.image}
                alt={content.recommendedItem.name}
                width={136}
                height={78}
                style={{
                  display: "block",
                  // 원본 비율을 유지한 채 상자 안에 맞춘다.
                  objectFit: "contain",
                }}
              />
            </div>
          ) : (
            <div
              aria-hidden
              style={{ height: 78, marginTop: 4, background: "#e5e5e5" }}
            />
          )}
          <dl
            style={{
              display: "grid",
              gridTemplateColumns: "auto 1fr",
              gap: "6px 6px",
              marginTop: 6,
              fontSize: 11,
            }}
          >
            <dt
              style={{
                padding: "0 8px",
                border: "1px solid #555",
                fontSize: 10,
                textAlign: "center",
              }}
            >
              이름
            </dt>
            <dd>{content.recommendedItem.name}</dd>
            <dt
              style={{
                padding: "0 8px",
                border: "1px solid #555",
                fontSize: 10,
                textAlign: "center",
              }}
            >
              가격
            </dt>
            <dd>{content.recommendedItem.price}</dd>
          </dl>
        </div>
      </article>
    );
  }

  return (
    <article
      style={{
        display: "flex",
        flexWrap: "wrap",
        alignItems: "center",
        gap: 16,
        padding: "20px 14px 20px 16px",
        border: "1px solid #999",
      }}
    >
      <div
        aria-hidden
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          flex: "0 0 112px",
          height: 112,
          background: "#d9d9d9",
          fontSize: 11,
          color: "#555",
        }}
      >
        콘텐츠 이미지
      </div>
      <div style={{ flex: "1 1 320px", minWidth: 0 }}>
        <p style={{ fontSize: 12, color: "#555" }}>컨텐츠 별 호/불호 평점</p>
        <h3 style={{ marginTop: 6, fontSize: 19, fontWeight: 700 }}>
          {/* 호/불호 공통 상세 페이지가 없어 좋아요 상세로 보낸다. */}
          <Link href={`/rating/${content.id}/like`}>{content.title}</Link>
        </h3>
        <p style={{ marginTop: 10, fontSize: 12, color: "#555" }}>
          {content.meta}
        </p>
        <div
          aria-hidden
          style={{ display: "flex", height: 8, marginTop: 10, background: "#ccc" }}
        >
          <span style={{ width: `${content.likeRatio}%`, background: "#555" }} />
        </div>
        <p
          style={{
            display: "flex",
            justifyContent: "space-between",
            marginTop: 6,
            paddingRight: 40,
            fontSize: 14,
            fontWeight: 700,
          }}
        >
          <span>좋아요 {content.likeRatio}%</span>
          <span>싫어요 {content.dislikeRatio}%</span>
        </p>
      </div>
    </article>
  );
}
