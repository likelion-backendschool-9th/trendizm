"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import RecommendForm from "@/app/item/[id]/_components/RecommendForm";
import { formatPrice } from "@/app/item/_lib/format";
import type { ItemRecommendation } from "@/app/item/_lib/types";
import { apiFetch } from "@/lib/api/client";
import { ApiError } from "@/lib/api/types";

type RecommendationsProps = {
  questionId: number;
  recommendations: ItemRecommendation[];
};

// 추천 아이템 목록. 잠긴 카드는 열기 버튼(100P)만 보이고, 열면 서버에서 다시 받아 열린 카드로 바뀐다.
// 로그인하지 않았으면 apiFetch가 로그인 화면으로 보낸다.
export default function Recommendations({
  questionId,
  recommendations,
}: RecommendationsProps) {
  const router = useRouter();
  const [unlockingId, setUnlockingId] = useState<number | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [formOpen, setFormOpen] = useState(false);

  async function unlock(item: ItemRecommendation) {
    if (!window.confirm(`${item.unlockPrice}P를 써서 이 추천 아이템을 열까요?`)) {
      return;
    }
    setUnlockingId(item.id);
    setMessage(null);
    try {
      await apiFetch(`/api/items/recommendations/${item.id}/unlock`, { method: "POST" });
      router.refresh();
    } catch (error) {
      if (error instanceof ApiError && error.code === "INSUFFICIENT_POINT_BALANCE") {
        setMessage("포인트가 부족해요. 호불호 평점에 후기를 처음 쓰면 10P를 받을 수 있어요.");
      } else if (error instanceof ApiError && error.status !== 401) {
        setMessage(error.message);
      }
    } finally {
      setUnlockingId(null);
    }
  }

  return (
    <section id="recommendations">
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: 20,
        }}
      >
        <div>
          <h2 style={{ fontSize: 20, fontWeight: 700 }}>
            추천 아이템 {recommendations.length}
          </h2>
          <p style={{ marginTop: 4, fontSize: 12, color: "#777" }}>
            잠긴 아이템은 100P로 열 수 있고, 낸 포인트는 추천한 사람에게 가요
          </p>
        </div>
        <button
          type="button"
          onClick={() => setFormOpen((open) => !open)}
          aria-expanded={formOpen}
          style={{
            padding: "10px 16px",
            border: "1px solid #111",
            background: formOpen ? "#111" : "#fff",
            color: formOpen ? "#fff" : "#111",
            fontSize: 13,
            fontWeight: 700,
            cursor: "pointer",
          }}
        >
          {formOpen ? "추천 닫기" : "+ 아이템 추천하기"}
        </button>
      </div>

      {formOpen && (
        <RecommendForm
          questionId={questionId}
          onDone={() => {
            setFormOpen(false);
            router.refresh();
          }}
        />
      )}

      {message && (
        <p
          role="alert"
          style={{
            marginBottom: 16,
            padding: "12px 16px",
            border: "1px solid #c00",
            color: "#c00",
            fontSize: 13,
          }}
        >
          {message}
        </p>
      )}

      {recommendations.length === 0 ? (
        <p
          style={{
            padding: "40px 0",
            border: "1px solid #ddd",
            textAlign: "center",
            fontSize: 14,
            color: "#777",
          }}
        >
          아직 추천이 없어요. 아는 아이템이 있다면 추천해 주세요.
        </p>
      ) : (
        <ul
          style={{
            display: "flex",
            flexDirection: "column",
            gap: 16,
            listStyle: "none",
          }}
        >
          {recommendations.map((item) => (
            <li
              key={item.id}
              style={{
                display: "flex",
                flexWrap: "wrap",
                gap: 28,
                padding: 20,
                border: "1px solid #111",
              }}
            >
              {item.locked ? (
                <LockedCard
                  item={item}
                  unlocking={unlockingId === item.id}
                  onUnlock={() => unlock(item)}
                />
              ) : (
                <OpenCard item={item} />
              )}
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}

function LockedCard({
  item,
  unlocking,
  onUnlock,
}: {
  item: ItemRecommendation;
  unlocking: boolean;
  onUnlock: () => void;
}) {
  return (
    <>
      <div
        aria-hidden
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          width: 160,
          height: 160,
          background: "#eee",
          border: "1px solid #ddd",
          fontSize: 32,
          filter: "blur(0.5px)",
        }}
      >
        🔒
      </div>
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          justifyContent: "center",
          gap: 12,
          flex: "1 1 240px",
        }}
      >
        <p style={{ fontSize: 13, color: "#777" }}>
          {item.authorNickname}님의 추천
        </p>
        <p style={{ fontSize: 16, fontWeight: 700 }}>
          이름·가격·판매처가 가려져 있어요
        </p>
        <button
          type="button"
          onClick={onUnlock}
          disabled={unlocking}
          style={{
            alignSelf: "flex-start",
            padding: "12px 20px",
            border: "none",
            background: "#111",
            color: "#fff",
            fontSize: 14,
            fontWeight: 700,
            cursor: unlocking ? "wait" : "pointer",
            opacity: unlocking ? 0.6 : 1,
          }}
        >
          {unlocking ? "여는 중..." : `🔓 ${item.unlockPrice}P로 열기`}
        </button>
      </div>
    </>
  );
}

function OpenCard({ item }: { item: ItemRecommendation }) {
  return (
    <>
      {item.imageUrl ? (
        // 사용자가 입력한 외부 이미지 주소라 next/image의 허용 도메인 설정을 쓸 수 없다
        // eslint-disable-next-line @next/next/no-img-element
        <img
          src={item.imageUrl}
          alt={item.title ?? "추천 아이템"}
          width={160}
          height={160}
          style={{
            display: "block",
            width: 160,
            height: 160,
            objectFit: "cover",
            border: "1px solid #ddd",
          }}
        />
      ) : (
        <div
          aria-hidden
          style={{
            width: 160,
            height: 160,
            background: "#f3f3f3",
            border: "1px solid #ddd",
          }}
        />
      )}
      <div style={{ flex: "1 1 240px", minWidth: 0 }}>
        <p style={{ fontSize: 11, color: "#777" }}>
          {item.authorNickname}님의 추천
          {item.mine && " (내 추천)"}
          {item.seller && ` · ${item.seller}`}
        </p>
        <h3 style={{ marginTop: 10, fontSize: 20, fontWeight: 700 }}>
          {item.title}
        </h3>
        {item.price !== null && (
          <p style={{ marginTop: 8, fontSize: 18, fontWeight: 700 }}>
            {formatPrice(item.price)}
          </p>
        )}
        {item.tags && item.tags.length > 0 && (
          <ul
            aria-label="아이템 특징"
            style={{
              display: "flex",
              flexWrap: "wrap",
              gap: 6,
              marginTop: 12,
              listStyle: "none",
            }}
          >
            {item.tags.map((tag) => (
              <li
                key={tag}
                style={{
                  padding: "4px 8px",
                  border: "1px solid #111",
                  fontSize: 11,
                }}
              >
                {tag}
              </li>
            ))}
          </ul>
        )}
        <p
          style={{
            marginTop: 12,
            fontSize: 14,
            lineHeight: 1.7,
            whiteSpace: "pre-wrap",
          }}
        >
          {item.description}
        </p>
        {item.linkUrl && (
          <a
            href={item.linkUrl}
            target="_blank"
            rel="noopener noreferrer"
            style={{
              display: "inline-block",
              marginTop: 16,
              padding: "12px 20px",
              background: "#111",
              color: "#fff",
              fontSize: 14,
              fontWeight: 700,
            }}
          >
            상품 보러가기 →
          </a>
        )}
      </div>
    </>
  );
}
