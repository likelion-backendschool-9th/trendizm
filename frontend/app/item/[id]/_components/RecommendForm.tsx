"use client";

import { useState, type FormEvent } from "react";
import { apiFetch } from "@/lib/api/client";
import { ApiError } from "@/lib/api/types";

type RecommendFormProps = {
  questionId: number;
  /** 등록에 성공하면 부른다 */
  onDone: () => void;
};

const FIELDS = [
  { name: "title", label: "아이템 이름", required: true, placeholder: "예: 포근 니트 양말" },
  { name: "seller", label: "판매처", required: false, placeholder: "예: 무신사" },
  { name: "price", label: "가격(원)", required: false, placeholder: "예: 12000" },
  { name: "tags", label: "태그", required: false, placeholder: "쉼표로 구분. 예: 화이트, 폭신한 재질" },
  { name: "imageUrl", label: "이미지 주소", required: false, placeholder: "https://..." },
  { name: "linkUrl", label: "상품 링크", required: false, placeholder: "https://..." },
] as const;

// 추천 아이템 등록 폼. POST /api/items/{id}/recommendations
// 입력값 오류(400 INVALID_INPUT)는 칸 아래에, 그 밖의 오류는 폼 위에 보여 준다.
export default function RecommendForm({ questionId, onDone }: RecommendFormProps) {
  const [error, setError] = useState<ApiError | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [priceError, setPriceError] = useState<string | null>(null);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const text = (name: string) => String(form.get(name) ?? "").trim();
    // "12,000원"처럼 써도 받는다. 그 밖에 숫자가 아닌 글자가 있으면 보내지 않고 알린다
    const price = text("price").replace(/[,\s원]/g, "");
    if (price && !/^\d+$/.test(price)) {
      setPriceError("가격은 숫자로 입력해 주세요. 예: 12000");
      return;
    }

    setPriceError(null);
    setSubmitting(true);
    setError(null);
    try {
      await apiFetch(`/api/items/${questionId}/recommendations`, {
        method: "POST",
        body: {
          title: text("title"),
          seller: text("seller") || null,
          price: price ? Number(price) : null,
          tags: text("tags")
            .split(",")
            .map((tag) => tag.trim())
            .filter(Boolean),
          description: text("description"),
          imageUrl: text("imageUrl") || null,
          linkUrl: text("linkUrl") || null,
        },
      });
      onDone();
    } catch (caught) {
      if (caught instanceof ApiError && caught.status !== 401) {
        setError(caught);
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form
      onSubmit={submit}
      style={{
        display: "flex",
        flexDirection: "column",
        gap: 14,
        marginBottom: 20,
        padding: 20,
        border: "1px solid #111",
        background: "#fafafa",
      }}
    >
      <p style={{ fontSize: 13, color: "#777" }}>
        등록한 아이템은 다른 사람에게 잠긴 상태로 보이고, 누가 열 때마다 100P가 쌓여요.
      </p>
      {error && error.errors.length === 0 && (
        <p role="alert" style={{ fontSize: 13, color: "#c00" }}>
          {error.message}
        </p>
      )}
      {FIELDS.map((field) => (
        <Field
          key={field.name}
          {...field}
          message={
            field.name === "price" && priceError ? priceError : error?.fieldMessage(field.name)
          }
          inputMode={field.name === "price" ? "numeric" : undefined}
        />
      ))}
      <label style={{ display: "flex", flexDirection: "column", gap: 6, fontSize: 13 }}>
        <span>
          설명 <span style={{ color: "#c00" }}>*</span>
        </span>
        <textarea
          name="description"
          required
          maxLength={2000}
          rows={4}
          placeholder="어떤 점이 질문과 잘 맞는지 알려 주세요"
          style={{ padding: 10, border: "1px solid #999", fontSize: 14, resize: "vertical" }}
        />
        {error?.fieldMessage("description") && (
          <span style={{ color: "#c00", fontSize: 12 }}>
            {error.fieldMessage("description")}
          </span>
        )}
      </label>
      <button
        type="submit"
        disabled={submitting}
        style={{
          alignSelf: "flex-end",
          padding: "12px 24px",
          border: "none",
          background: "#111",
          color: "#fff",
          fontSize: 14,
          fontWeight: 700,
          cursor: submitting ? "wait" : "pointer",
          opacity: submitting ? 0.6 : 1,
        }}
      >
        {submitting ? "등록 중..." : "추천 등록"}
      </button>
    </form>
  );
}

function Field({
  name,
  label,
  required,
  placeholder,
  message,
  inputMode,
}: {
  name: string;
  label: string;
  required: boolean;
  placeholder: string;
  message?: string;
  inputMode?: "numeric";
}) {
  return (
    <label style={{ display: "flex", flexDirection: "column", gap: 6, fontSize: 13 }}>
      <span>
        {label} {required && <span style={{ color: "#c00" }}>*</span>}
      </span>
      <input
        name={name}
        required={required}
        placeholder={placeholder}
        inputMode={inputMode}
        style={{ height: 40, padding: "0 10px", border: "1px solid #999", fontSize: 14 }}
      />
      {message && <span style={{ color: "#c00", fontSize: 12 }}>{message}</span>}
    </label>
  );
}
