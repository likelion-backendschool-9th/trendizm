"use client";

import { useRouter } from "next/navigation";
import { CONTENT_SORTS, type ContentSort } from "@/app/rating/_lib/types";

type SortSelectProps = {
  /** 지금 목록 주소. 예: /rating/cate/media */
  basePath: string;
  /** 지금 고른 진행 상태. 정렬을 바꿔도 유지한다 */
  status?: string;
  sort: ContentSort;
};

// 콘텐츠 목록 정렬 선택. 바꾸면 첫 페이지부터 다시 보여 준다(진행 상태 필터는 유지).
export default function SortSelect({ basePath, status, sort }: SortSelectProps) {
  const router = useRouter();

  function change(next: ContentSort) {
    const query = new URLSearchParams();
    if (status) {
      query.set("status", status);
    }
    if (next !== "latest") {
      query.set("sort", next);
    }
    const search = query.toString();
    router.push(search ? `${basePath}?${search}` : basePath);
  }

  return (
    <label style={{ display: "flex", alignItems: "center", gap: 8, fontSize: 12 }}>
      정렬
      <select
        value={sort}
        onChange={(event) => change(event.target.value as ContentSort)}
        style={{
          padding: "5px 8px",
          border: "1px solid #222",
          borderRadius: 6,
          background: "#fff",
          fontSize: 12,
        }}
      >
        {CONTENT_SORTS.map((option) => (
          <option key={option.slug} value={option.slug}>
            {option.name}
          </option>
        ))}
      </select>
    </label>
  );
}
