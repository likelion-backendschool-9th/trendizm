import Image from "next/image";
import Link from "next/link";

// 와이어프레임: 트렌드 아이템 - 상세페이지 (item/[id]) - 수정본

const POST = {
  category: { slug: "fashion", name: "패션" },
  subcategory: { slug: "clothing", name: "의류" },
  status: "답변 대기중",
  title: "양말 추천해주세요ㅠㅠ",
  author: "작성자",
  createdAt: "2026.11.13",
  viewCount: 132,
  body: "양말을 사고 싶은데, 제가 원하는 스타일이나 저한테 딱 맞는 양말이 없네요... 세련된 스타일의 양말 중 화이트 계열 색상의 따뜻한 느낌에 재질은 폭신폭신한 양말 어디 없나요~?",
  curiousCount: 0,
  commentCount: 3,
};

// 보는 사람의 상태. ⋯ 메뉴에 상태에 맞는 동작만 보인다.
type Viewer = "owner" | "guest" | "member";

const VIEWER: Viewer = "member";

const ACTIONS: Record<Viewer, string[]> = {
  owner: ["수정하기", "삭제하기"],
  guest: ["신고하기", "차단하기"],
  member: ["신고하기", "차단하기", "이 사용자 글 숨기기"],
};

const RECOMMENDED_ITEMS = [
  {
    id: 1,
    image: "/images/item/socks.jpg",
    seller: "판매처",
    name: "아이템 이름",
    price: "10,000원",
    tags: ["화이트", "폭신한 재질"],
    description:
      "아이템에 대한 설명입니다. 이 아이템은 이러쿵저러쿵 하고 어쩌구저쩌구가 있습니다.",
  },
];

const COMMENTS = [
  {
    id: 1,
    author: "작성자",
    // 아이템을 추천한 댓글이면 true
    isRecommendation: true,
    createdAt: "작성일",
    body: "댓글 내용",
    likeCount: 0,
    replies: [],
  },
  {
    id: 2,
    author: "작성자",
    isRecommendation: false,
    createdAt: "작성일",
    body: "댓글 내용",
    likeCount: 0,
    replies: [
      {
        id: 21,
        author: "작성자",
        createdAt: "작성일",
        body: "답글 내용",
        likeCount: 0,
      },
    ],
  },
];

const ADJACENT_POSTS = [
  { label: "이전 글", id: 3, title: "이전 질문 제목" },
  { label: "다음 글", id: 2, title: "다음 질문 제목" },
];

// 질문자가 찾는 조건
const CONDITIONS = [
  { label: "스타일", value: "세련된" },
  { label: "색상", value: "화이트" },
  { label: "느낌", value: "따뜻한" },
  { label: "재질", value: "폭신폭신한" },
];

const SIMILAR_POSTS = [
  { id: 4, title: "관련 질문 제목", commentCount: 3 },
  { id: 5, title: "관련 질문 제목", commentCount: 1 },
  { id: 6, title: "관련 질문 제목", commentCount: 5 },
];

// 같은 상위 카테고리의 하위 카테고리
const SUBCATEGORIES = [
  { slug: "clothing", name: "의류" },
  { slug: "shoes", name: "신발" },
  { slug: "bags", name: "가방" },
  { slug: "eyewear-accessories", name: "안경·액세서리" },
  { slug: "watches-jewelry", name: "시계·주얼리" },
];

export default function ItemDetailPage() {
  const [currentItem] = RECOMMENDED_ITEMS;

  return (
    <main
      style={{
        width: "100%",
        maxWidth: 1280,
        margin: "0 auto",
        padding: "40px 80px 80px",
        background: "#fff",
        color: "#111",
      }}
    >
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          gap: 16,
          marginBottom: 28,
        }}
      >
        <nav aria-label="경로" style={{ fontSize: 13, color: "#777" }}>
          <Link href="/item">‹ 트렌드 아이템</Link>
          <span style={{ margin: "0 8px" }}>/</span>
          <Link href={`/item/cate/${POST.category.slug}`}>
            {POST.category.name}
          </Link>
          <span style={{ margin: "0 8px" }}>/</span>
          <Link
            href={`/item/cate/${POST.category.slug}/${POST.subcategory.slug}`}
            style={{ fontWeight: 700, color: "#111" }}
          >
            {POST.subcategory.name}
          </Link>
        </nav>
        {/* TODO: 질문하기 화면 연결 */}
        <button
          type="button"
          style={{
            padding: "14px 20px",
            border: "none",
            background: "#111",
            color: "#fff",
            fontSize: 14,
            fontWeight: 700,
            cursor: "pointer",
          }}
        >
          + 질문하기
        </button>
      </div>

      <div
        style={{
          display: "flex",
          flexWrap: "wrap",
          alignItems: "flex-start",
          gap: 30,
        }}
      >
        <div
          style={{
            display: "flex",
            flexDirection: "column",
            gap: 20,
            flex: "1 1 600px",
            minWidth: 0,
          }}
        >
          <article style={{ padding: "32px 36px", border: "1px solid #111" }}>
            <div style={{ display: "flex", gap: 8 }}>
              <span
                style={{
                  padding: "4px 10px",
                  border: "1px solid #111",
                  fontSize: 12,
                }}
              >
                {POST.subcategory.name}
              </span>
              <span
                style={{
                  padding: "4px 10px",
                  border: "1px solid #111",
                  fontSize: 12,
                }}
              >
                {POST.status}
              </span>
            </div>
            <h1 style={{ marginTop: 14, fontSize: 30, fontWeight: 700 }}>
              {POST.title}
            </h1>
            <div
              style={{
                display: "flex",
                alignItems: "center",
                gap: 12,
                marginTop: 18,
                fontSize: 13,
                color: "#777",
              }}
            >
              <span
                aria-hidden
                style={{ width: 32, height: 32, background: "#e5e5e5" }}
              />
              <span style={{ fontWeight: 700, color: "#111" }}>
                {POST.author}
              </span>
              · {POST.createdAt} · 조회 {POST.viewCount}
            </div>
            <p style={{ marginTop: 18, fontSize: 16, lineHeight: 1.7 }}>
              {POST.body}
            </p>

            <div
              style={{
                display: "flex",
                alignItems: "center",
                gap: 8,
                marginTop: 18,
                paddingTop: 16,
                borderTop: "1px solid #ddd",
              }}
            >
              {/* TODO: 나도 궁금해요, 저장, 공유 연결 */}
              <button
                type="button"
                style={{
                  padding: "8px 14px",
                  border: "1px solid #111",
                  background: "#fff",
                  fontSize: 13,
                  cursor: "pointer",
                }}
              >
                👍 나도 궁금해요 {POST.curiousCount}
              </button>
              <a
                href="#comments"
                style={{
                  padding: "8px 14px",
                  border: "1px solid #111",
                  fontSize: 13,
                }}
              >
                💬 댓글 {POST.commentCount}
              </a>
              <div
                style={{
                  display: "flex",
                  alignItems: "center",
                  gap: 24,
                  marginLeft: "auto",
                  fontSize: 13,
                  color: "#777",
                }}
              >
                <button
                  type="button"
                  style={{
                    border: "none",
                    background: "none",
                    color: "inherit",
                    cursor: "pointer",
                  }}
                >
                  저장
                </button>
                <button
                  type="button"
                  style={{
                    border: "none",
                    background: "none",
                    color: "inherit",
                    cursor: "pointer",
                  }}
                >
                  공유
                </button>
                <details style={{ position: "relative" }}>
                  <summary
                    aria-label="더보기"
                    style={{ listStyle: "none", cursor: "pointer" }}
                  >
                    ⋯
                  </summary>
                  {/* TODO: 각 동작 연결 */}
                  <ul
                    style={{
                      position: "absolute",
                      right: 0,
                      top: 24,
                      zIndex: 1,
                      minWidth: 160,
                      listStyle: "none",
                      border: "1px solid #111",
                      background: "#fff",
                    }}
                  >
                    {ACTIONS[VIEWER].map((action) => (
                      <li key={action}>
                        <button
                          type="button"
                          style={{
                            width: "100%",
                            padding: "10px 14px",
                            border: "none",
                            background: "none",
                            textAlign: "left",
                            fontSize: 13,
                            cursor: "pointer",
                          }}
                        >
                          {action}
                        </button>
                      </li>
                    ))}
                  </ul>
                </details>
              </div>
            </div>
          </article>

          <section>
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                marginBottom: 20,
              }}
            >
              <div>
                <h2 style={{ fontSize: 20, fontWeight: 700 }}>추천 아이템</h2>
                <p style={{ marginTop: 4, fontSize: 12, color: "#777" }}>
                  질문 조건에 맞춰 골랐어요
                </p>
              </div>
              {/* TODO: 캐러셀 연결 */}
              <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
                <button
                  type="button"
                  aria-label="이전 추천 아이템"
                  style={{
                    width: 40,
                    height: 40,
                    border: "1px solid #111",
                    background: "#fff",
                    cursor: "pointer",
                  }}
                >
                  ‹
                </button>
                <span style={{ fontSize: 13, color: "#777" }}>
                  1 / {RECOMMENDED_ITEMS.length}
                </span>
                <button
                  type="button"
                  aria-label="다음 추천 아이템"
                  style={{
                    width: 40,
                    height: 40,
                    border: "1px solid #111",
                    background: "#fff",
                    cursor: "pointer",
                  }}
                >
                  ›
                </button>
              </div>
            </div>

            <div
              style={{
                display: "flex",
                flexWrap: "wrap",
                gap: 28,
                padding: 20,
                border: "1px solid #111",
              }}
            >
              <div style={{ position: "relative", flexShrink: 0 }}>
                <Image
                  src={currentItem.image}
                  alt={currentItem.name}
                  width={220}
                  height={260}
                  style={{
                    display: "block",
                    // 원본 비율과 달라도 찌그러지지 않게 잘라서 채운다.
                    objectFit: "cover",
                    border: "1px solid #ddd",
                  }}
                />
                {/* TODO: 찜하기 연결 */}
                <button
                  type="button"
                  aria-label="찜하기"
                  style={{
                    position: "absolute",
                    top: 12,
                    right: 12,
                    width: 36,
                    height: 36,
                    border: "1px solid #111",
                    background: "#fff",
                    fontSize: 16,
                    cursor: "pointer",
                  }}
                >
                  ♡
                </button>
              </div>
              <div style={{ flex: "1 1 240px", minWidth: 0 }}>
                <p style={{ fontSize: 11, color: "#777" }}>
                  {currentItem.seller}
                </p>
                <h3 style={{ marginTop: 10, fontSize: 20, fontWeight: 700 }}>
                  {currentItem.name}
                </h3>
                <p style={{ marginTop: 10, fontSize: 20, fontWeight: 700 }}>
                  {currentItem.price}
                </p>
                <ul
                  aria-label="아이템 특징"
                  style={{
                    display: "flex",
                    gap: 6,
                    marginTop: 12,
                    listStyle: "none",
                  }}
                >
                  {currentItem.tags.map((tag) => (
                    <li
                      key={tag}
                      style={{
                        padding: "4px 8px",
                        border: "1px solid #111",
                        fontSize: 11,
                      }}
                    >
                      {tag}
                    </li>
                  ))}
                </ul>
                <p style={{ marginTop: 12, fontSize: 14, lineHeight: 1.7 }}>
                  {currentItem.description}
                </p>
                {/* TODO: 판매처 상품 페이지 연결 */}
                <button
                  type="button"
                  style={{
                    marginTop: 16,
                    padding: "14px 20px",
                    border: "none",
                    background: "#111",
                    color: "#fff",
                    fontSize: 14,
                    fontWeight: 700,
                    cursor: "pointer",
                  }}
                >
                  상품 보러가기 →
                </button>
              </div>
            </div>
          </section>

          <section id="comments" style={{ marginTop: 12 }}>
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                marginBottom: 20,
              }}
            >
              <h2 style={{ fontSize: 20, fontWeight: 700 }}>
                댓글 {POST.commentCount}
              </h2>
              {/* TODO: 정렬 연결 */}
              <div
                style={{ display: "flex", padding: 3, border: "1px solid #111" }}
              >
                {["공감순", "최신순"].map((sort, index) => (
                  <button
                    key={sort}
                    type="button"
                    aria-pressed={index === 0}
                    style={{
                      padding: "6px 14px",
                      border: "none",
                      background: index === 0 ? "#111" : "#fff",
                      color: index === 0 ? "#fff" : "#111",
                      fontSize: 12,
                      cursor: "pointer",
                    }}
                  >
                    {sort}
                  </button>
                ))}
              </div>
            </div>

            <div style={{ border: "1px solid #111" }}>
              {/* TODO: 댓글 작성 요청 연결 */}
              <form
                style={{
                  display: "flex",
                  alignItems: "center",
                  gap: 12,
                  padding: 20,
                  borderBottom: "1px solid #ddd",
                }}
              >
                <span
                  aria-hidden
                  style={{
                    flexShrink: 0,
                    width: 36,
                    height: 36,
                    background: "#e5e5e5",
                  }}
                />
                <input
                  type="text"
                  name="comment"
                  placeholder="아는 아이템이 있다면 추천해 주세요"
                  style={{
                    flex: 1,
                    minWidth: 0,
                    height: 46,
                    padding: "0 14px",
                    border: "1px solid #111",
                    fontSize: 14,
                  }}
                />
                <button
                  type="submit"
                  style={{
                    height: 44,
                    padding: "0 20px",
                    border: "none",
                    background: "#111",
                    color: "#fff",
                    fontSize: 14,
                    fontWeight: 700,
                    cursor: "pointer",
                  }}
                >
                  등록
                </button>
              </form>

              <ul style={{ listStyle: "none" }}>
                {COMMENTS.map((comment, index) => (
                  <li
                    key={comment.id}
                    style={{
                      display: "flex",
                      gap: 14,
                      padding: "22px 24px",
                      borderTop: index === 0 ? "none" : "1px solid #ddd",
                    }}
                  >
                    <span
                      aria-hidden
                      style={{
                        flexShrink: 0,
                        width: 40,
                        height: 40,
                        background: "#e5e5e5",
                      }}
                    />
                    <div style={{ flex: 1, minWidth: 0 }}>
                      <p
                        style={{
                          display: "flex",
                          alignItems: "center",
                          gap: 8,
                          fontSize: 13,
                        }}
                      >
                        <span style={{ fontWeight: 700 }}>{comment.author}</span>
                        {comment.isRecommendation && (
                          <span
                            style={{
                              padding: "2px 6px",
                              border: "1px solid #111",
                              fontSize: 11,
                            }}
                          >
                            아이템 추천
                          </span>
                        )}
                        <span style={{ color: "#777" }}>
                          {comment.createdAt}
                        </span>
                      </p>
                      <p style={{ marginTop: 8, fontSize: 14 }}>
                        {comment.body}
                      </p>
                      {/* TODO: 공감, 답글 달기 연결 */}
                      <div
                        style={{
                          display: "flex",
                          gap: 24,
                          marginTop: 12,
                          fontSize: 12,
                          color: "#777",
                        }}
                      >
                        <button
                          type="button"
                          style={{
                            border: "none",
                            background: "none",
                            color: "inherit",
                            cursor: "pointer",
                          }}
                        >
                          공감 {comment.likeCount}
                        </button>
                        <button
                          type="button"
                          style={{
                            border: "none",
                            background: "none",
                            color: "inherit",
                            cursor: "pointer",
                          }}
                        >
                          답글 달기
                        </button>
                      </div>

                      {comment.replies.length > 0 && (
                        <ul
                          style={{
                            marginTop: 18,
                            listStyle: "none",
                            border: "1px solid #ddd",
                            background: "#f7f7f7",
                          }}
                        >
                          {comment.replies.map((reply) => (
                            <li
                              key={reply.id}
                              style={{ display: "flex", gap: 12, padding: 16 }}
                            >
                              <span style={{ color: "#777" }}>↳</span>
                              <span
                                aria-hidden
                                style={{
                                  flexShrink: 0,
                                  width: 32,
                                  height: 32,
                                  background: "#e5e5e5",
                                }}
                              />
                              <div>
                                <p style={{ fontSize: 13 }}>
                                  <span style={{ fontWeight: 700 }}>
                                    {reply.author}
                                  </span>{" "}
                                  <span style={{ color: "#777" }}>
                                    {reply.createdAt}
                                  </span>
                                </p>
                                <p style={{ marginTop: 6, fontSize: 14 }}>
                                  {reply.body}
                                </p>
                                <button
                                  type="button"
                                  style={{
                                    marginTop: 10,
                                    border: "none",
                                    background: "none",
                                    color: "#777",
                                    fontSize: 12,
                                    cursor: "pointer",
                                  }}
                                >
                                  공감 {reply.likeCount}
                                </button>
                              </div>
                            </li>
                          ))}
                        </ul>
                      )}
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          </section>

          <nav aria-label="글 이동">
            <ul style={{ listStyle: "none", border: "1px solid #111" }}>
              {ADJACENT_POSTS.map((post, index) => (
                <li
                  key={post.label}
                  style={{
                    display: "flex",
                    gap: 32,
                    padding: "18px 24px",
                    borderTop: index === 0 ? "none" : "1px solid #ddd",
                    fontSize: 14,
                  }}
                >
                  <span style={{ color: "#777" }}>{post.label}</span>
                  <Link href={`/item/${post.id}`}>{post.title}</Link>
                </li>
              ))}
            </ul>
            <Link
              href="/item"
              style={{
                display: "block",
                width: 144,
                margin: "28px auto 0",
                padding: "12px 0",
                border: "1px solid #111",
                textAlign: "center",
                fontSize: 14,
              }}
            >
              ☰ 목록으로
            </Link>
          </nav>
        </div>

        <aside
          style={{
            display: "flex",
            flexDirection: "column",
            gap: 20,
            flex: "0 1 356px",
          }}
        >
          <section style={{ padding: 24, border: "1px solid #111" }}>
            <h2 style={{ fontSize: 17, fontWeight: 700 }}>
              질문자가 찾는 조건
            </h2>
            <dl
              style={{
                display: "flex",
                flexDirection: "column",
                gap: 12,
                marginTop: 16,
                fontSize: 13,
              }}
            >
              {CONDITIONS.map((condition) => (
                <div key={condition.label} style={{ display: "flex" }}>
                  <dt style={{ width: 76, color: "#777" }}>
                    {condition.label}
                  </dt>
                  <dd style={{ fontWeight: 700 }}>{condition.value}</dd>
                </div>
              ))}
            </dl>
            {/* TODO: 조건으로 추천 요청 연결 */}
            <button
              type="button"
              style={{
                width: "100%",
                marginTop: 18,
                padding: "13px 0",
                border: "1px solid #111",
                background: "#fff",
                fontSize: 14,
                fontWeight: 700,
                cursor: "pointer",
              }}
            >
              이 조건으로 아이템 추천하기
            </button>
          </section>

          <section style={{ padding: 24, border: "1px solid #111" }}>
            <h2 style={{ fontSize: 17, fontWeight: 700 }}>비슷한 질문</h2>
            <ul style={{ marginTop: 8, listStyle: "none" }}>
              {SIMILAR_POSTS.map((post, index) => (
                <li
                  key={post.id}
                  style={{
                    padding: "14px 0",
                    borderTop: index === 0 ? "none" : "1px solid #ddd",
                  }}
                >
                  <Link href={`/item/${post.id}`} style={{ fontSize: 14 }}>
                    {post.title}
                  </Link>
                  <p style={{ marginTop: 6, fontSize: 12, color: "#777" }}>
                    {POST.category.name} · {POST.subcategory.name} · 댓글{" "}
                    {post.commentCount}
                  </p>
                </li>
              ))}
            </ul>
          </section>

          <section style={{ padding: 24, border: "1px solid #111" }}>
            <h2 style={{ fontSize: 17, fontWeight: 700 }}>
              {POST.category.name} 카테고리
            </h2>
            <ul
              style={{
                display: "flex",
                flexWrap: "wrap",
                gap: 8,
                marginTop: 16,
                listStyle: "none",
              }}
            >
              {SUBCATEGORIES.map((subcategory) => {
                const selected = subcategory.slug === POST.subcategory.slug;

                return (
                  <li key={subcategory.slug}>
                    <Link
                      href={`/item/cate/${POST.category.slug}/${subcategory.slug}`}
                      aria-current={selected ? "page" : undefined}
                      style={{
                        display: "block",
                        padding: "7px 12px",
                        border: "1px solid #111",
                        background: selected ? "#111" : "#fff",
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
          </section>
        </aside>
      </div>
    </main>
  );
}
