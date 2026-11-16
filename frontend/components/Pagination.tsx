import Link from "next/link";
import type { ReactNode } from "react";

type PaginationProps = {
  // 1부터 시작하는 현재 페이지
  currentPage: number;
  totalPages: number;
};

// 한 번에 이어서 보여 줄 페이지 번호 개수
const WINDOW_SIZE = 5;

// 공용 페이지네이션. 예: ‹ 1 2 3 4 5 … 10 ›
// 페이지 번호는 현재 주소에 ?page=N을 붙인 링크로 이동한다.
// TODO: 각 페이지에서 searchParams의 page 값을 읽어 목록을 바꾸도록 연결
export default function Pagination({ currentPage, totalPages }: PaginationProps) {
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
      <PageLink page={currentPage - 1} disabled={currentPage <= 1} label="이전 페이지">
        ‹
      </PageLink>

      {start > 1 && (
        <>
          <PageLink page={1}>1</PageLink>
          {start > 2 && <Gap />}
        </>
      )}
      {pages.map((page) => (
        <PageLink key={page} page={page} current={page === currentPage}>
          {page}
        </PageLink>
      ))}
      {end < totalPages && (
        <>
          {end < totalPages - 1 && <Gap />}
          <PageLink page={totalPages}>{totalPages}</PageLink>
        </>
      )}

      <PageLink
        page={currentPage + 1}
        disabled={currentPage >= totalPages}
        label="다음 페이지"
      >
        ›
      </PageLink>
    </nav>
  );
}

function PageLink({
  page,
  current = false,
  disabled = false,
  label,
  children,
}: {
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
      href={{ query: { page } }}
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
