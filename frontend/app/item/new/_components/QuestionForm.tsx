"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState, type FormEvent } from "react";
import { ITEM_CATEGORIES } from "@/app/item/_lib/categories";
import type { ItemQuestion } from "@/app/item/_lib/types";
import { apiFetch } from "@/lib/api/client";
import { ApiError } from "@/lib/api/types";

// 질문 글 작성 폼. POST /api/items (소분류 slug만 보내면 대분류는 서버가 정한다)
// 쓰다가 로그인이 풀려 내용을 잃지 않게, 처음 열 때 로그인 여부를 먼저 확인한다.
export default function QuestionForm() {
  const router = useRouter();
  const [categorySlug, setCategorySlug] = useState(ITEM_CATEGORIES[0].slug);
  const [error, setError] = useState<ApiError | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const category =
    ITEM_CATEGORIES.find((candidate) => candidate.slug === categorySlug) ?? ITEM_CATEGORIES[0];

  useEffect(() => {
    // 로그인하지 않았으면 apiFetch가 로그인 화면(/login?next=/item/new)으로 보낸다
    apiFetch("/api/auth/me").catch(() => {});
  }, []);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const formElement = event.currentTarget;
    const form = new FormData(formElement);
    setSubmitting(true);
    setError(null);
    try {
      const created = await apiFetch<ItemQuestion>("/api/items", {
        method: "POST",
        body: {
          subcategory: String(form.get("subcategory") ?? ""),
          title: String(form.get("title") ?? "").trim(),
          content: String(form.get("content") ?? "").trim(),
        },
      });
      // Next가 이 페이지 상태를 기억해 두므로, 다시 "질문하기"로 들어왔을 때 빈 폼이 보이도록 초기화한다
      formElement.reset();
      setCategorySlug(ITEM_CATEGORIES[0].slug);
      setSubmitting(false);
      router.push(`/item/${created.id}`);
    } catch (caught) {
      if (caught instanceof ApiError && caught.status !== 401) {
        setError(caught);
      }
      setSubmitting(false);
    }
  }

  const labelStyle = { display: "flex", flexDirection: "column", gap: 6, fontSize: 13 } as const;
  const inputStyle = { height: 42, padding: "0 10px", border: "1px solid #999", fontSize: 14 };

  return (
    <form
      onSubmit={submit}
      style={{ display: "flex", flexDirection: "column", gap: 18, marginTop: 24 }}
    >
      {error && error.errors.length === 0 && (
        <p role="alert" style={{ fontSize: 13, color: "#c00" }}>
          {error.message}
        </p>
      )}
      <div style={{ display: "flex", gap: 12, flexWrap: "wrap" }}>
        <label style={{ ...labelStyle, flex: "1 1 200px" }}>
          대분류
          <select
            value={categorySlug}
            onChange={(event) => setCategorySlug(event.target.value)}
            style={inputStyle}
          >
            {ITEM_CATEGORIES.map((candidate) => (
              <option key={candidate.slug} value={candidate.slug}>
                {candidate.name}
              </option>
            ))}
          </select>
        </label>
        <label style={{ ...labelStyle, flex: "1 1 200px" }}>
          소분류
          {/* 대분류가 바뀌면 소분류 첫 항목부터 다시 고르도록 key로 새로 그린다 */}
          <select key={category.slug} name="subcategory" style={inputStyle}>
            {category.subcategories.map((subcategory) => (
              <option key={subcategory.slug} value={subcategory.slug}>
                {subcategory.name}
              </option>
            ))}
          </select>
        </label>
      </div>
      <label style={labelStyle}>
        제목
        <input
          name="title"
          required
          maxLength={100}
          placeholder="예: 편하면서 예쁜 운동화 추천해 주세요"
          style={inputStyle}
        />
        {error?.fieldMessage("title") && (
          <span style={{ color: "#c00", fontSize: 12 }}>{error.fieldMessage("title")}</span>
        )}
      </label>
      <label style={labelStyle}>
        내용
        <textarea
          name="content"
          required
          maxLength={5000}
          rows={8}
          placeholder="원하는 스타일, 색상, 가격대 등을 적어 주세요"
          style={{ padding: 10, border: "1px solid #999", fontSize: 14, resize: "vertical" }}
        />
        {error?.fieldMessage("content") && (
          <span style={{ color: "#c00", fontSize: 12 }}>{error.fieldMessage("content")}</span>
        )}
      </label>
      <button
        type="submit"
        disabled={submitting}
        style={{
          alignSelf: "flex-end",
          padding: "14px 28px",
          border: "none",
          background: "#111",
          color: "#fff",
          fontSize: 14,
          fontWeight: 700,
          cursor: submitting ? "wait" : "pointer",
          opacity: submitting ? 0.6 : 1,
        }}
      >
        {submitting ? "올리는 중..." : "질문 올리기"}
      </button>
    </form>
  );
}
