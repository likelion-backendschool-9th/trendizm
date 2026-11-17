"use client";

import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { Suspense, type ReactNode } from "react";

type PaginationProps = {
  // 1부터 시작하는 현재 페이지
  currentPage: number;
  totalPages: number;
};

// 한 번에 이어서 보여 줄 페이지 번호 개수
const WINDOW_SIZE = 5;

// 공용 페이지네이션. 예: ‹ 1 2 3 4 5 … 10 ›
// 지금 주소의 다른 쿼리(정렬·필터·검색어)는 그대로 두고 page만 바꾼 링크로 이동한다.
// TODO: 각 페이지에서 searchParams의 page 값을 읽어 목록을 바꾸도록 연결
export default function Pagination(props: PaginationProps) {
  // 미리 만들어 둔 HTML에서는 쿼리를 알 수 없어서, 먼저 page만 붙인 링크를 보여 준다.
  // 브라우저에서 실제 쿼리를 읽으면 다른 조건을 유지한 링크로 바뀐다.
  return (
    <Suspense fallback={<PaginationLinks {...props} query="" />}>
      <PaginationWithQuery {...props} />
    </Suspense>
  );
}

function PaginationWithQuery(props: PaginationProps) {
  const searchParams = useSearchParams();
  return <PaginationLinks {...props} query={searchParams.toString()} />;
}

function PaginationLinks({
  currentPage,
  totalPages,
  query,
}: PaginationProps & { query: string }) {
  if (totalPages < 1) {
    return null;
  }

  // 현재 페이지가 가운데 오도록 번호 묶음을 정하고, 양 끝은 범위 안으로 맞춘다.
  const start = Math.max(
    1,
    Math.min(currentPage - Math.floor(WINDOW_SIZE / 2), totalPages - WINDOW_SIZE + 1),
  );
  const end = Math.min(totalPages, start + WINDOW_SIZE - 1);
  const pages = Array.from({ length: end - start + 1 }, (_, index) => start + index);

  return (
    <nav
      aria-label="페이지네이션"
      style={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        gap: 8,
        marginTop: 24,
        fontSize: 13,
      }}
    >
      <PageLink
        query={query}
        page={currentPage - 1}
        disabled={currentPage <= 1}
        label="이전 페이지"
      >
        ‹
      </PageLink>

      {start > 1 && (
        <>
          <PageLink query={query} page={1}>
            1
          </PageLink>
          {start > 2 && <Gap />}
        </>
      )}
      {pages.map((page) => (
        <PageLink key={page} query={query} page={page} current={page === currentPage}>
          {page}
        </PageLink>
      ))}
      {end < totalPages && (
        <>
          {end < totalPages - 1 && <Gap />}
          <PageLink query={query} page={totalPages}>
            {totalPages}
          </PageLink>
        </>
      )}

      <PageLink
        query={query}
        page={currentPage + 1}
        disabled={currentPage >= totalPages}
        label="다음 페이지"
      >
        ›
      </PageLink>
    </nav>
  );
}

// 지금 쿼리를 복사한 뒤 page만 바꾼다. ?tag=a&tag=b처럼 반복되는 키도 유지된다.
function pageHref(query: string, page: number) {
  const params = new URLSearchParams(query);
  params.set("page", String(page));
  return `?${params}`;
}

function PageLink({
  query,
  page,
  current = false,
  disabled = false,
  label,
  children,
}: {
  // 지금 주소의 쿼리 문자열 (? 없이)
  query: string;
  page: number;
  current?: boolean;
  disabled?: boolean;
  // 화살표처럼 글자만으로 뜻이 안 보일 때 스크린리더용 이름
  label?: string;
  children: ReactNode;
}) {
  const style = {
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    width: 32,
    height: 32,
    borderRadius: 4,
    background: current ? "#222" : "transparent",
    color: current ? "#fff" : disabled ? "#bbb" : "#111",
    fontWeight: current ? 700 : 400,
  };

  // 첫 페이지의 '이전', 마지막 페이지의 '다음'은 누를 수 없게 글자만 둔다.
  if (disabled) {
    return (
      <span aria-label={label} aria-disabled style={style}>
        {children}
      </span>
    );
  }

  return (
    <Link
      href={pageHref(query, page)}
      aria-label={label}
      aria-current={current ? "page" : undefined}
      style={style}
    >
      {children}
    </Link>
  );
}

function Gap() {
  return (
    <span aria-hidden style={{ width: 32, textAlign: "center" }}>
      …
    </span>
  );
}
