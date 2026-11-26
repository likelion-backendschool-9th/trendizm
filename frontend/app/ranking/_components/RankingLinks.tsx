"use client";

import { useState } from "react";
import type { RankingLink } from "@/app/ranking/_lib/types";

/** 한 번에 보여 줄 링크 수. 더보기·뒤로가기를 누르면 이만큼씩 넘긴다 */
const PAGE_SIZE = 3;

/** 썸네일 주소에서 이미지를 불러오지 못할 때 대신 보여 줄 이미지 (public/images/notfound.jpg) */
const NOT_FOUND_IMAGE = "/images/notfound.jpg";

type RankingLinksProps = {
  links: RankingLink[];
};

// 순위 키워드의 관련 링크. API는 링크를 전부 주고, 화면은 3개씩 보여 준다.
// 오른쪽 더보기는 다음 3개, 왼쪽 뒤로가기는 앞의 3개로 바꾼다(아래에 이어 붙이지 않는다).
// 넘길 쪽이 없는 버튼은 숨기되 자리는 남겨, 넘길 때 링크 칸 너비가 바뀌지 않게 한다. 링크가 3개 이하면 버튼 자리도 없다.
// 썸네일이 없거나(플랫폼 정책상 표시할 수 없는 경우 등) 불러오지 못하면 NOT_FOUND_IMAGE를 보여 준다.
export default function RankingLinks({ links }: RankingLinksProps) {
  const [page, setPage] = useState(0);
  const pageCount = Math.ceil(links.length / PAGE_SIZE);
  const start = page * PAGE_SIZE;

  return (
    <div style={{ display: "flex", alignItems: "flex-start", gap: 20, marginTop: 16 }}>
      {pageCount > 1 && (
        <PageButton
          label="뒤로가기"
          arrow="‹"
          invisible={page === 0}
          onClick={() => setPage(page - 1)}
        />
      )}
      <ul
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(3, minmax(0, 1fr))",
          gap: 20,
          flex: 1,
          listStyle: "none",
        }}
      >
        {links.slice(start, start + PAGE_SIZE).map((link) => (
          <li key={link.id}>
            <a
              href={link.url}
              target="_blank"
              rel="noopener noreferrer"
              style={{ display: "block", color: "inherit", textDecoration: "none" }}
            >
              <RankingThumbnail src={link.thumbnailUrl} />
              <span
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "baseline",
                  gap: 8,
                  marginTop: 10,
                }}
              >
                {/* 제목은 게시물 캡션 전체라 길고 줄바꿈이 섞여 있어 두 줄까지만 보여 준다. */}
                <span
                  style={{
                    display: "-webkit-box",
                    WebkitLineClamp: 2,
                    WebkitBoxOrient: "vertical",
                    overflow: "hidden",
                    minWidth: 0,
                    fontSize: 14,
                    fontWeight: 700,
                  }}
                >
                  {link.title ?? "제목 없음"}
                </span>
                {link.publishedAt && (
                  <time dateTime={link.publishedAt} style={{ flexShrink: 0, fontSize: 11 }}>
                    {formatDate(link.publishedAt)}
                  </time>
                )}
              </span>
              {link.description && (
                // 설명은 세 줄까지만 보여 주고, 넘치면 셋째 줄 끝에 말줄임표(…)를 붙여 자른다.
                // 띄어쓰기 없는 긴 글자(주소 등)도 칸 안에서 줄을 바꿔 세 줄 안에 들어오게 한다.
                <span
                  style={{
                    display: "-webkit-box",
                    WebkitLineClamp: 3,
                    WebkitBoxOrient: "vertical",
                    overflow: "hidden",
                    overflowWrap: "anywhere",
                    marginTop: 6,
                    fontSize: 11,
                    color: "#555",
                  }}
                >
                  {link.description}
                </span>
              )}
            </a>
          </li>
        ))}
      </ul>
      {pageCount > 1 && (
        <PageButton
          label="더보기"
          arrow="›"
          invisible={page === pageCount - 1}
          onClick={() => setPage(page + 1)}
        />
      )}
    </div>
  );
}

// 관련 링크 썸네일. 플랫폼 CDN의 외부 이미지라 next/image의 허용 도메인 설정을 쓸 수 없어서 <img>를 쓴다.
// 썸네일 주소가 없거나(null), 만료되거나 지워져 불러오지 못하면 NOT_FOUND_IMAGE를 보여 준다.
// 그 파일도 불러오지 못하면(파일이 빠진 경우 등) 배경색(회색 칸)만 보인다. 제목이 바로 아래에 있어 alt는 비운다.
function RankingThumbnail({ src }: { src: string | null }) {
  const [failed, setFailed] = useState(false);

  return (
    // eslint-disable-next-line @next/next/no-img-element
    <img
      // 서버에서 그린 이미지가 화면 준비(하이드레이션) 전에 실패하면 onError가 불리지 않으므로,
      // 붙을 때 이미 실패한 이미지(불러오기는 끝났는데 크기가 0)인지 한 번 더 본다.
      ref={(img) => {
        if (img?.complete && img.naturalWidth === 0) {
          setFailed(true);
        }
      }}
      src={failed || !src ? NOT_FOUND_IMAGE : src}
      alt=""
      loading="lazy"
      referrerPolicy="no-referrer"
      onError={() => setFailed(true)}
      style={{
        display: "block",
        width: "100%",
        height: 100,
        objectFit: "cover",
        background: "#d9d9d9",
      }}
    />
  );
}

type PageButtonProps = {
  label: string;
  arrow: string;
  /** 넘길 쪽이 없으면 숨긴다. 자리는 남고, 화면·키보드·스크린리더에서는 빠진다(visibility: hidden) */
  invisible: boolean;
  onClick: () => void;
};

function PageButton({ label, arrow, invisible, onClick }: PageButtonProps) {
  return (
    <button
      type="button"
      onClick={onClick}
      style={{
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        flexShrink: 0,
        gap: 8,
        marginTop: 30,
        border: "none",
        background: "none",
        fontSize: 15,
        cursor: "pointer",
        visibility: invisible ? "hidden" : "visible",
      }}
    >
      <span
        aria-hidden
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          width: 40,
          height: 40,
          border: "1px solid #999",
          borderRadius: "50%",
          background: "#f5f5f5",
          fontSize: 18,
        }}
      >
        {arrow}
      </span>
      {label}
    </button>
  );
}

/** 2026-11-20T18:00:00 → 2026.11.20. 시간대 없는 날짜라 Date로 바꾸지 않고 그대로 자른다 */
function formatDate(value: string): string {
  return value.slice(0, 10).replaceAll("-", ".");
}
