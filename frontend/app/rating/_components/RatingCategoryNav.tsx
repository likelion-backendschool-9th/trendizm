import Image from "next/image";
import Link from "next/link";
import { RATING_CATEGORIES } from "@/app/rating/_lib/categories";

type RatingCategoryNavProps = {
  // 없으면 '전체'(/rating)가 선택된 것으로 보여 준다.
  cate?: string;
  subcate?: string;
};

// 와이어프레임: 컨텐츠 별 호/불호 평점 (rating) - 수정본의 카테고리 선택
// 아이콘 대분류 한 줄과 선택한 대분류의 소분류 한 줄.
export default function RatingCategoryNav({ cate, subcate }: RatingCategoryNavProps) {
  const current = RATING_CATEGORIES.find((category) => category.slug === cate);
  const subcategories = current
    ? [
        { slug: "", name: "전체", href: `/rating/cate/${current.slug}` },
        ...current.subcategories.map((subcategory) => ({
          ...subcategory,
          href: `/rating/cate/${current.slug}/${subcategory.slug}`,
        })),
      ]
    : [];

  const tileStyle = (selected: boolean) =>
    ({
      display: "flex",
      flexDirection: "column",
      alignItems: "center",
      justifyContent: "center",
      gap: 10,
      height: 88,
      border: "1px solid #222",
      borderRadius: 8,
      background: selected ? "#111" : "#fff",
      color: selected ? "#fff" : "#111",
      fontSize: 13,
      fontWeight: 700,
    }) as const;

  return (
    <nav
      aria-label="카테고리"
      style={{ padding: "20px 24px 16px", border: "1px solid #222", borderRadius: 12 }}
    >
      <ul
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(88px, 1fr))",
          gap: 8,
          paddingBottom: current ? 20 : 0,
          borderBottom: current ? "1px solid #222" : "none",
          listStyle: "none",
        }}
      >
        <li>
          <Link
            href="/rating"
            aria-current={!current ? "page" : undefined}
            style={tileStyle(!current)}
          >
            <span aria-hidden style={{ fontSize: 18, lineHeight: "21px" }}>
              ☰
            </span>
            전체
          </Link>
        </li>
        {RATING_CATEGORIES.map((category) => {
          const selected = category === current;

          return (
            <li key={category.slug}>
              <Link
                href={`/rating/cate/${category.slug}`}
                aria-current={selected ? "page" : undefined}
                style={tileStyle(selected)}
              >
                <Image
                  src={category.icon}
                  alt=""
                  width={25}
                  height={21}
                  style={{
                    // 아이콘 선이 짙은 회색이라 선택된 검은 칸에서는 흰색으로 바꾼다.
                    filter: selected ? "brightness(0) invert(1)" : undefined,
                  }}
                />
                {category.name}
              </Link>
            </li>
          );
        })}
      </ul>

      {current && (
        <div style={{ display: "flex", alignItems: "center", gap: 30, marginTop: 16 }}>
          <p style={{ flexShrink: 0, fontSize: 12, color: "#555" }}>{current.name}</p>
          <ul
            aria-label="하위 카테고리"
            style={{ display: "flex", flexWrap: "wrap", gap: 8, listStyle: "none" }}
          >
            {subcategories.map((subcategory) => {
              const selected = (subcate ?? "") === subcategory.slug;

              return (
                <li key={subcategory.href}>
                  <Link
                    href={subcategory.href}
                    aria-current={selected ? "page" : undefined}
                    style={{
                      display: "block",
                      padding: "6px 12px",
                      border: "1px solid #222",
                      borderRadius: 8,
                      background: selected ? "#111" : "#fff",
                      color: selected ? "#fff" : "#555",
                      fontSize: 12,
                      fontWeight: selected ? 700 : 400,
                    }}
                  >
                    {subcategory.name}
                  </Link>
                </li>
              );
            })}
          </ul>
        </div>
      )}
    </nav>
  );
}
