"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import RankingLinks from "@/app/ranking/_components/RankingLinks";
import type { Ranking, RankingTabs } from "@/app/ranking/_lib/types";

/** 순위별 상세 카드를 다음 순위로 넘기는 간격 (10초) */
const ROTATION_INTERVAL_MS = 10_000;

type RankingListProps = {
  /** 플랫폼·카테고리 탭 (GET /api/rankings/tabs) */
  tabs: RankingTabs;
  /** 고른 플랫폼 slug */
  platform: string;
  /** 고른 카테고리 slug. 카테고리가 하나도 없으면 없다 */
  category?: string;
  /** 1~10위. 아직 수집 전이면 빈 배열 */
  rankings: Ranking[];
  /** 이 순위를 수집한 시각(GET /api/rankings의 collectedAt). 아직 수집 전이면 null이거나 없다 */
  collectedAt?: string | null;
};

// 와이어프레임: 플랫폼별 트렌드 순위
// /ranking/[platform]/[category]가 쓰고, 카테고리가 하나도 없으면 /ranking/[platform]도 쓴다.
// 순위별 상세 카드는 하나만 펼치고 ROTATION_INTERVAL_MS마다 다음 순위를 펼치므로 클라이언트 컴포넌트다.
export default function RankingList({
  tabs,
  platform,
  category,
  rankings,
  collectedAt,
}: RankingListProps) {
  return (
    <main
      style={{
        width: "100%",
        maxWidth: 800,
        margin: "0 auto",
        padding: "48px 30px 64px",
        background: "#fff",
        color: "#111",
      }}
    >
      {/* 와이어프레임에는 제목이 보이지 않아 화면에서는 숨기고 스크린리더에만 읽힌다. */}
      <h1
        style={{
          position: "absolute",
          width: 1,
          height: 1,
          overflow: "hidden",
          clip: "rect(0 0 0 0)",
          whiteSpace: "nowrap",
        }}
      >
        플랫폼별 트렌드 순위
      </h1>

      {/* 수집 시각. 수집 전이면 글자 없이 자리만 남겨 탭 위치가 바뀌지 않게 한다. */}
      <p style={{ minHeight: "1.5em", marginBottom: 20, textAlign: "right" }}>
        {collectedAt && (
          <time dateTime={collectedAt}>{formatCollectedAt(collectedAt)} 기준</time>
        )}
      </p>

      <nav aria-label="플랫폼">
        <ul style={{ display: "flex", flexWrap: "wrap", gap: 10, listStyle: "none" }}>
          {tabs.platforms.map((item) => {
            const selected = item.slug === platform;

            return (
              <li key={item.slug}>
                {/* 플랫폼을 바꿔도 고른 카테고리는 그대로 둔다. */}
                <Link
                  href={category ? `/ranking/${item.slug}/${category}` : `/ranking/${item.slug}`}
                  aria-current={selected ? "page" : undefined}
                  style={{
                    display: "block",
                    width: 118,
                    padding: "6px 0",
                    border: "1px solid #111",
                    background: selected ? "#111" : "#fff",
                    color: selected ? "#fff" : "#111",
                    textAlign: "center",
                    fontSize: 18,
                  }}
                >
                  {item.label}
                </Link>
              </li>
            );
          })}
        </ul>
      </nav>

      <nav aria-label="카테고리" style={{ marginTop: 10 }}>
        <ul style={{ display: "flex", flexWrap: "wrap", gap: 6, listStyle: "none" }}>
          {tabs.categories.map((item) => {
            const selected = item.slug === category;

            return (
              <li key={item.id}>
                <Link
                  href={`/ranking/${platform}/${item.slug}`}
                  aria-current={selected ? "page" : undefined}
                  style={{
                    display: "block",
                    // 'AI'처럼 짧은 영문 항목도 한글 항목과 같은 크기로 보이게 한다.
                    // 줄 높이를 고정해 글꼴 차이로 높이가 달라지지 않게 하고,
                    // 최소 너비는 가장 짧은 한글 항목(반려동물) 정도로 둔다.
                    minWidth: 64,
                    padding: "6px 12px",
                    lineHeight: "16px",
                    textAlign: "center",
                    border: "1px solid #999",
                    borderRadius: 999,
                    background: selected ? "#111" : "#fff",
                    color: selected ? "#fff" : "#111",
                    fontSize: 14,
                    fontWeight: 700,
                  }}
                >
                  {item.title}
                </Link>
              </li>
            );
          })}
        </ul>
      </nav>

      {rankings.length === 0 ? (
        <p style={{ marginTop: 40, textAlign: "center", fontSize: 16, color: "#555" }}>
          아직 수집한 순위가 없습니다.
        </p>
      ) : (
        // 플랫폼·카테고리를 바꾸면 1위 카드부터 다시 펼친다.
        <RankingSections key={`${platform}/${category}`} rankings={rankings} />
      )}
    </main>
  );
}

/** 2026-11-23T12:04:14 → 2026-11-23 12:04:14. 시간대 없는 시각이라 Date로 바꾸지 않고 글자만 바꾼다 */
function formatCollectedAt(value: string): string {
  return value.slice(0, 19).replace("T", " ");
}

function RankingSections({ rankings }: { rankings: Ranking[] }) {
  // 펼친 카드의 순서(0부터). 카드는 한 번에 하나만 펼친다.
  const [openIndex, setOpenIndex] = useState(0);
  // 카드 위에 마우스가 있거나 키보드 포커스가 카드 안에 있으면 넘기지 않는다(읽거나 누르는 중에 접히지 않게).
  const [hovered, setHovered] = useState(false);
  const [keyboardFocused, setKeyboardFocused] = useState(false);
  const paused = hovered || keyboardFocused;

  // 펼친 카드가 바뀔 때마다(자동으로든 눌러서든) 10초를 새로 센다. 마지막 순위 다음은 1위로 돌아간다.
  useEffect(() => {
    if (paused || rankings.length < 2) {
      return;
    }
    const timer = setTimeout(
      () => setOpenIndex((index) => (index + 1) % rankings.length),
      ROTATION_INTERVAL_MS,
    );
    return () => clearTimeout(timer);
  }, [openIndex, paused, rankings.length]);

  return (
    <>
      <section style={{ marginTop: 24 }}>
        <h2
          style={{
            position: "absolute",
            width: 1,
            height: 1,
            overflow: "hidden",
            clip: "rect(0 0 0 0)",
            whiteSpace: "nowrap",
          }}
        >
          순위
        </h2>
        <ol
          style={{
            display: "grid",
            gridTemplateColumns: "repeat(2, minmax(0, 1fr))",
            gap: "8px 24px",
            padding: "0 10px",
            listStyle: "none",
          }}
        >
          {rankings.map((ranking) => (
            <li
              key={ranking.id}
              style={{ display: "flex", alignItems: "baseline", gap: 16, fontSize: 22 }}
            >
              <span style={{ width: 32, fontSize: 28, fontWeight: 700 }}>
                {ranking.rank}
              </span>
              {ranking.keyword}
            </li>
          ))}
        </ol>
      </section>

      <section
        onMouseEnter={() => setHovered(true)}
        onMouseLeave={() => setHovered(false)}
        // 마우스로 누른 포커스는 멈추지 않고, 키보드(Tab)로 옮긴 포커스(:focus-visible)만 멈춘다.
        onFocus={(event) => setKeyboardFocused(event.target.matches(":focus-visible"))}
        onBlur={(event) => {
          if (!event.currentTarget.contains(event.relatedTarget)) {
            setKeyboardFocused(false);
          }
        }}
        style={{ display: "flex", flexDirection: "column", gap: 20, marginTop: 40 }}
      >
        <h2
          style={{
            position: "absolute",
            width: 1,
            height: 1,
            overflow: "hidden",
            clip: "rect(0 0 0 0)",
            whiteSpace: "nowrap",
          }}
        >
          순위별 상세
        </h2>
        {/* TODO: 차트 안에 채팅 들어감 */}
        {rankings.map((ranking, index) => {
          // 펼친 카드는 요약 전체를, 접힌 카드는 한 줄만 보여 준다.
          const open = index === openIndex;

          return (
            <details
              key={ranking.id}
              open={open}
              style={{ padding: "24px 24px 28px", border: "1px solid #111" }}
            >
              <summary
                onClick={(event) => {
                  // 브라우저 기본 동작(이 카드만 열고 닫기) 대신 누른 카드를 펼치고 나머지를 접는다.
                  // 펼친 카드를 다시 눌러도 접지 않는다(항상 하나는 펼쳐 둔다).
                  event.preventDefault();
                  setOpenIndex(index);
                }}
                style={{ listStyle: "none", cursor: "pointer" }}
              >
                <span
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                  }}
                >
                  <span style={{ display: "flex", alignItems: "baseline", gap: 24 }}>
                    <span style={{ fontSize: 40, fontWeight: 700 }}>{ranking.rank}</span>
                    <span style={{ fontSize: 36 }}>{ranking.keyword}</span>
                  </span>
                  <span aria-hidden style={{ fontSize: 18, color: "#ccc" }}>
                    {open ? "▲" : "▼"}
                  </span>
                </span>
                <span
                  style={{
                    display: "flex",
                    flexDirection: open ? "column" : "row",
                    alignItems: open ? "flex-start" : "center",
                    gap: open ? 14 : 10,
                    marginTop: 10,
                  }}
                >
                  <span
                    style={{
                      flexShrink: 0,
                      padding: "2px 10px",
                      border: "1px solid #111",
                      fontSize: open ? 16 : 14,
                    }}
                  >
                    AI 요약
                  </span>
                  <span
                    style={{
                      ...(open
                        ? { fontSize: 18, lineHeight: 1.5 }
                        : {
                            minWidth: 0,
                            overflow: "hidden",
                            textOverflow: "ellipsis",
                            whiteSpace: "nowrap",
                            fontSize: 13,
                          }),
                      color: ranking.summary ? undefined : "#888",
                    }}
                  >
                    {ranking.summary ?? "AI 요약을 준비하고 있습니다."}
                  </span>
                </span>
              </summary>

              {ranking.links.length > 0 && (
                <>
                  <h3
                    style={{
                      display: "inline-block",
                      marginTop: 18,
                      padding: "2px 16px",
                      border: "1px solid #111",
                      fontSize: 16,
                      fontWeight: 400,
                    }}
                  >
                    관련 링크
                  </h3>
                  <RankingLinks links={ranking.links} />
                </>
              )}
            </details>
          );
        })}
      </section>
    </>
  );
}
