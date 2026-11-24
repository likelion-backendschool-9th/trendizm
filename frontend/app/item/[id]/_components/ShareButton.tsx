"use client";

import { useState } from "react";

type ShareButtonProps = {
  title: string;
};

// 공유. 휴대폰처럼 공유 창(navigator.share)이 있으면 그걸 열고, 없으면 지금 주소를 클립보드에 복사한다.
export default function ShareButton({ title }: ShareButtonProps) {
  const [message, setMessage] = useState<string | null>(null);

  async function share() {
    const url = window.location.href;
    try {
      if (navigator.share) {
        await navigator.share({ title, url });
        return;
      }
      await navigator.clipboard.writeText(url);
      setMessage("링크를 복사했어요");
    } catch (error) {
      // 공유 창을 닫은 경우(AbortError)는 알리지 않는다
      if (error instanceof DOMException && error.name === "AbortError") {
        return;
      }
      setMessage("복사하지 못했어요. 주소창의 링크를 복사해 주세요");
    }
    window.setTimeout(() => setMessage(null), 2000);
  }

  return (
    <span style={{ position: "relative" }}>
      <button
        type="button"
        onClick={share}
        style={{
          padding: "8px 14px",
          border: "1px solid #111",
          background: "#fff",
          fontSize: 13,
          cursor: "pointer",
        }}
      >
        🔗 공유
      </button>
      {message && (
        <span
          role="status"
          style={{
            position: "absolute",
            top: "calc(100% + 6px)",
            right: 0,
            padding: "6px 10px",
            background: "#111",
            color: "#fff",
            fontSize: 12,
            whiteSpace: "nowrap",
          }}
        >
          {message}
        </span>
      )}
    </span>
  );
}
