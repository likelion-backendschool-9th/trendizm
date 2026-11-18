import Image from "next/image";
import Link from "next/link";
import CategoryNav from "@/app/item/_components/CategoryNav";
import Pagination from "@/components/Pagination";

export type ItemCategory = {
  slug: string;
  name: string;
  subcategories: { slug: string; name: string }[];
};

export type RecommendedItem = {
  // public 폴더 기준 경로
  image?: string;
  name: string;
  price: string;
};

export type ItemPost = {
  id: number;
  subcategory: string;
  // solved: 해결됨, waiting: 답변 대기중
  status: "solved" | "waiting";
  title: string;
  body: string;
  author: string;
  createdAt: string;
  commentCount: number;
  viewCount: number;
  // 추천 아이템. 잠겨 있으면 100P를 내고 해금해야 보인다.
  recommendation:
    | { locked: true; count: number }
    | { locked: false; items: [RecommendedItem, ...RecommendedItem[]] };
};

type ItemListProps = {
  categories: ItemCategory[];
  cate?: string;
  subcate?: string;
  totalCount: number;
  posts: ItemPost[];
};

const STATUS_LABEL: Record<ItemPost["status"], string> = {
  solved: "해결됨",
  waiting: "답변 대기중",
};

// 와이어프레임: 트렌드 아이템 (item) - 수정본
// /item, /item/cate/[cate], /item/cate/[cate]/[subcate]가 함께 쓴다.
export default function ItemList({
  categories,
  cate,
  subcate,
  totalCount,
  posts,
}: ItemListProps) {
  // 화면 위쪽 첫 이미지는 가장 크게 보이는 요소(LCP)라 늦게 불러오지 않는다.
  const firstImagePostId = posts.find(
    (post) => !post.recommendation.locked && post.recommendation.items[0].image,
  )?.id;

  return (
    <main
      style={{
        width: "100%",
        maxWidth: 800,
        margin: "0 auto",
        padding: "24px 32px 48px",
        background: "#fff",
        color: "#111",
      }}
    >
      <header
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "flex-start",
          gap: 16,
          marginBottom: 24,
        }}
      >
        <div>
          <h1 style={{ fontSize: 26, fontWeight: 700 }}>트렌드 아이템</h1>
          <p style={{ marginTop: 6, fontSize: 13, color: "#777" }}>
            원하는 아이템을 물어보고, 딱 맞는 추천을 받아보세요
          </p>
        </div>
        {/* TODO: 질문하기 화면 연결 */}
        <button
          type="button"
          style={{
            padding: "8px 24px",
            border: "1px solid #999",
            background: "#fff",
            fontSize: 13,
            cursor: "pointer",
          }}
        >
          + 질문하기
        </button>
      </header>

      <CategoryNav categories={categories} cate={cate} subcate={subcate} />

      {/* TODO: 필터, 정렬 연결 */}
      <div
        style={{
          display: "flex",
          alignItems: "center",
          gap: 44,
          margin: "18px 0 16px",
          fontSize: 13,
        }}
      >
        <p>질문 {totalCount}개</p>
        <label style={{ display: "flex", alignItems: "center", gap: 8 }}>
          <input type="checkbox" name="waitingOnly" />
          답변 대기중만 보기
        </label>
        <label
          style={{
            display: "flex",
            alignItems: "center",
            gap: 16,
            marginLeft: "auto",
          }}
        >
          정렬
          <select
            name="sort"
            defaultValue="latest"
            style={{
              width: 90,
              padding: "8px 10px",
              border: "1px solid #999",
              background: "#fff",
              fontSize: 12,
            }}
          >
            <option value="latest">최신순</option>
          </select>
        </label>
      </div>

      <section
        aria-label="질문 목록"
        style={{ display: "flex", flexDirection: "column", gap: 16 }}
      >
        {posts.map((post) => (
          <article
            key={post.id}
            style={{
              display: "flex",
              gap: 24,
              padding: 16,
              border: "1px solid #999",
            }}
          >
            <div
              style={{
                display: "flex",
                flexDirection: "column",
                flex: 1,
                minWidth: 0,
              }}
            >
              <div style={{ display: "flex", gap: 6 }}>
                <span
                  style={{
                    padding: "4px 10px",
                    border: "1px solid #999",
                    fontSize: 12,
                  }}
                >
                  {post.subcategory}
                </span>
                <span
                  style={{
                    padding: "4px 10px",
                    border: "1px solid #999",
                    background: "#eee",
                    fontSize: 12,
                  }}
                >
                  {STATUS_LABEL[post.status]}
                </span>
              </div>
              <h2 style={{ marginTop: 16, fontSize: 20, fontWeight: 700 }}>
                <Link href={`/item/${post.id}`}>{post.title}</Link>
              </h2>
              <p
                style={{
                  marginTop: 8,
                  fontSize: 13,
                  lineHeight: 1.6,
                  // 두 줄이 넘으면 말줄임표로 자른다.
                  display: "-webkit-box",
                  WebkitLineClamp: 2,
                  WebkitBoxOrient: "vertical",
                  overflow: "hidden",
                }}
              >
                {post.body}
              </p>
              <p
                style={{
                  marginTop: "auto",
                  paddingTop: 24,
                  fontSize: 11,
                  color: "#777",
                }}
              >
                {post.author} | {post.createdAt} | 댓글 {post.commentCount} |
                조회수 {post.viewCount}
              </p>
            </div>

            <aside
              aria-label="추천 아이템"
              style={{ flexShrink: 0, width: 184, fontSize: 11 }}
            >
              {post.recommendation.locked ? (
                <>
                  {/* TODO: 100P 해금 요청 연결 */}
                  <button
                    type="button"
                    style={{
                      display: "flex",
                      flexDirection: "column",
                      alignItems: "center",
                      justifyContent: "center",
                      gap: 10,
                      width: "100%",
                      height: 106,
                      border: "1px solid #999",
                      background: "#eee",
                      fontSize: 12,
                      fontWeight: 700,
                      cursor: "pointer",
                    }}
                  >
                    <span style={{ fontSize: 22 }}>🔒</span>
                    100P로 추천 아이템 보기
                  </button>
                  <div
                    style={{
                      display: "flex",
                      justifyContent: "space-between",
                      marginTop: 14,
                      color: "#777",
                    }}
                  >
                    <p>추천 아이템 {post.recommendation.count}개</p>
                    <p>잠금</p>
                  </div>
                </>
              ) : (
                <>
                  {post.recommendation.items[0].image && (
                    <Image
                      src={post.recommendation.items[0].image}
                      alt={post.recommendation.items[0].name}
                      width={184}
                      height={106}
                      loading={post.id === firstImagePostId ? "eager" : "lazy"}
                      style={{
                        display: "block",
                        // 너비와 높이를 함께 고정하고, 원본 비율과 달라도 찌그러지지 않게 잘라서 채운다.
                        width: "100%",
                        height: 106,
                        objectFit: "cover",
                        border: "1px solid #999",
                      }}
                    />
                  )}
                  <div
                    style={{
                      display: "flex",
                      justifyContent: "space-between",
                      alignItems: "flex-end",
                      marginTop: 14,
                    }}
                  >
                    <div>
                      <p style={{ fontSize: 12 }}>
                        {post.recommendation.items[0].name}
                      </p>
                      <p style={{ marginTop: 6 }}>
                        {post.recommendation.items[0].price}
                      </p>
                    </div>
                    <span
                      style={{
                        padding: "3px 6px",
                        border: "1px solid #999",
                        background: "#eee",
                      }}
                    >
                      추천 {post.recommendation.items.length}
                    </span>
                  </div>
                </>
              )}
            </aside>
          </article>
        ))}
      </section>

      {/* TODO: 실제 현재 페이지와 전체 페이지 수 연결 */}
      <Pagination currentPage={1} totalPages={10} />
    </main>
  );
}
