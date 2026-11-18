import Image from "next/image";
import Link from "next/link";
import type { RatingCategory } from "@/app/rating/_components/RatingList";

type RatingCategoryNavProps = {
  categories: RatingCategory[];
  // 없으면 첫 번째 대분류의 '전체'가 선택된 것으로 보여 준다.
  cate?: string;
  subcate?: string;
};

// 와이어프레임: 컨텐츠 별 호/불호 평점 (rating) - 수정본의 카테고리 선택
// 아이콘 대분류 한 줄과 선택한 대분류의 소분류 한 줄.
export default function RatingCategoryNav({
  categories,
  cate,
  subcate,
}: RatingCategoryNavProps) {
  const current =
    categories.find((category) => category.slug === cate) ?? categories[0];
  const subcategories = [
    { slug: "", name: "전체", href: `/rating/cate/${current.slug}` },
    ...current.subcategories.map((subcategory) => ({
      ...subcategory,
      href: `/rating/cate/${current.slug}/${subcategory.slug}`,
    })),
  ];

  return (
    <nav
      aria-label="카테고리"
      style={{ padding: "20px 24px 16px", border: "1px solid #222", borderRadius: 12 }}
    >
      <ul
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(96px, 1fr))",
          gap: 8,
          paddingBottom: 20,
          borderBottom: "1px solid #222",
          listStyle: "none",
        }}
      >
        {categories.map((category) => {
          const selected = category === current;

          return (
            <li key={category.slug}>
              <Link
                href={`/rating/cate/${category.slug}`}
                aria-current={selected ? "page" : undefined}
                style={{
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
                }}
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

      <div
        style={{
          display: "flex",
          alignItems: "center",
          gap: 30,
          marginTop: 16,
        }}
      >
        <p style={{ flexShrink: 0, fontSize: 12, color: "#555" }}>
          {current.name}
        </p>
        <ul
          aria-label="하위 카테고리"
          style={{ display: "flex", flexWrap: "wrap", gap: 8, listStyle: "none" }}
        >
          {subcategories.map((subcategory) => {
            const selected = (subcate ?? "") === subcategory.slug;

            return (
              <li key={subcategory.slug}>
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
    </nav>
  );
}
