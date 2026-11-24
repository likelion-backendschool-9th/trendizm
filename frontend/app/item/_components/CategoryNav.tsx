import Link from "next/link";
import { ITEM_CATEGORIES } from "@/app/item/_lib/categories";

type CategoryNavProps = {
  // 없으면 '전체'(/item)가 선택된 것으로 보여 준다.
  cate?: string;
  subcate?: string;
};

// 와이어프레임: 카테고리 상태 · 대분류별
// 대분류 탭 한 줄과 선택한 대분류의 소분류 버튼 한 줄.
export default function CategoryNav({ cate, subcate }: CategoryNavProps) {
  const current = ITEM_CATEGORIES.find((category) => category.slug === cate);
  const tabs = [
    { slug: "", name: "전체", href: "/item" },
    ...ITEM_CATEGORIES.map((category) => ({
      slug: category.slug,
      name: category.name,
      href: `/item/cate/${category.slug}`,
    })),
  ];
  const subcategories = current
    ? [
        { slug: "", name: "전체", href: `/item/cate/${current.slug}` },
        ...current.subcategories.map((subcategory) => ({
          ...subcategory,
          href: `/item/cate/${current.slug}/${subcategory.slug}`,
        })),
      ]
    : [];

  return (
    <nav
      aria-label="카테고리"
      style={{ padding: "16px 16px 20px", border: "1px solid #ccc" }}
    >
      <ul
        style={{
          display: "flex",
          flexWrap: "wrap",
          gap: 28,
          listStyle: "none",
          borderBottom: "1px solid #ddd",
        }}
      >
        {tabs.map((tab) => {
          const selected = (current?.slug ?? "") === tab.slug;

          return (
            // 선택 밑줄이 아래 구분선 위에 겹치도록 1px 내린다.
            <li key={tab.href} style={{ marginBottom: -1 }}>
              <Link
                href={tab.href}
                aria-current={selected ? "page" : undefined}
                style={{
                  display: "block",
                  paddingBottom: 10,
                  lineHeight: "18px",
                  fontSize: 13,
                  fontWeight: selected ? 700 : 400,
                  color: selected ? "#111" : "#777",
                  borderBottom: selected
                    ? "3px solid #111"
                    : "3px solid transparent",
                }}
              >
                {tab.name}
              </Link>
            </li>
          );
        })}
      </ul>

      {subcategories.length > 0 && (
        <ul
          aria-label="하위 카테고리"
          style={{
            display: "flex",
            flexWrap: "wrap",
            gap: 6,
            marginTop: 17,
            listStyle: "none",
          }}
        >
          {subcategories.map((subcategory) => {
            const selected = (subcate ?? "") === subcategory.slug;

            return (
              <li key={subcategory.href}>
                <Link
                  href={subcategory.href}
                  aria-current={selected ? "page" : undefined}
                  style={{
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    minWidth: 56,
                    height: 28,
                    padding: "0 16px",
                    border: selected ? "1px solid #222" : "1px solid #999",
                    background: selected ? "#222" : "#fff",
                    color: selected ? "#fff" : "#111",
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
      )}
    </nav>
  );
}
