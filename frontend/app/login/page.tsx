"use client";

import Link from "next/link";
import { useState, type FormEvent } from "react";
import { apiFetch } from "@/lib/api/client";
import { ApiError, type LoginMember } from "@/lib/api/types";

// 와이어프레임: 로그인 (login)
// 오류 상태: 필수값 또는 이메일 형식 오류는 해당 입력 필드 아래에 표시한다.
// 로그인에 실패하면(이메일·비밀번호 불일치, 이용 제한 등) 폼 위에 표시한다.

type FieldName = "email" | "password";
type Errors = Partial<Record<FieldName | "form", string>>;

const FIELDS: { name: FieldName; type: string; label: string; placeholder: string; autoComplete: string }[] = [
  {
    name: "email",
    type: "email",
    label: "이메일",
    placeholder: "이메일 주소를 입력하세요",
    autoComplete: "email",
  },
  {
    name: "password",
    type: "password",
    label: "비밀번호",
    placeholder: "비밀번호를 입력하세요",
    autoComplete: "current-password",
  },
];

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

/**
 * 로그인 후 돌아갈 주소. 같은 사이트 안의 경로만 쓴다(열린 리다이렉트 방지).
 * "//evil.com", "/\evil.com"은 브라우저가 다른 사이트로 해석하므로 막는다.
 * useSearchParams 대신 제출 순간에 주소창을 읽는다. 훅을 쓰면 운영 빌드에서 Suspense가 필요하다.
 */
function nextPath(): string {
  const next = new URLSearchParams(window.location.search).get("next");
  if (!next || !next.startsWith("/") || next.startsWith("//") || next.includes("\\")) {
    return "/";
  }
  return next;
}

/** 서버에 보내기 전에 입력칸을 검사한다. 메시지는 백엔드 검증 메시지와 같게 둔다 */
function validate(email: string, password: string): Errors {
  const errors: Errors = {};
  if (!email) {
    errors.email = "이메일을 입력해 주세요.";
  } else if (!EMAIL_PATTERN.test(email)) {
    errors.email = "이메일 형식이 아닙니다.";
  }
  if (!password) {
    errors.password = "비밀번호를 입력해 주세요.";
  }
  return errors;
}

export default function LoginPage() {
  const [errors, setErrors] = useState<Errors>({});
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (submitting) {
      return;
    }
    const form = new FormData(event.currentTarget);
    const email = String(form.get("email") ?? "").trim();
    const password = String(form.get("password") ?? "");
    const rememberMe = form.get("remember") === "on";

    const found = validate(email, password);
    setErrors(found);
    if (Object.keys(found).length > 0) {
      return;
    }

    setSubmitting(true);
    try {
      await apiFetch<LoginMember>("/api/auth/login", {
        method: "POST",
        body: { email, password, rememberMe },
      });
      // 헤더 등 화면 전체가 로그인 상태로 다시 그려지도록 전체 이동한다
      window.location.assign(nextPath());
    } catch (error) {
      setSubmitting(false);
      if (error instanceof ApiError) {
        const emailError = error.fieldMessage("email");
        const passwordError = error.fieldMessage("password");
        setErrors(
          emailError || passwordError ? { email: emailError, password: passwordError } : { form: error.message },
        );
      } else {
        setErrors({ form: "요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요." });
      }
    }
  }

  return (
    <main
      style={{
        width: "100%",
        maxWidth: 552,
        margin: "0 auto",
        padding: "38px 16px 64px",
        background: "#fff",
        color: "#111",
      }}
    >
      <nav aria-label="경로" style={{ fontSize: 12, color: "#555" }}>
        <Link href="/">홈</Link> / 로그인
      </nav>
      <h1 style={{ marginTop: 22, fontSize: 28, fontWeight: 700 }}>로그인</h1>
      <p style={{ marginTop: 14, fontSize: 14, color: "#555" }}>
        계정으로 들어가 관심 있는 트렌드를 이어서 확인하세요.
      </p>

      {/* 브라우저 기본 말풍선 대신 입력칸 아래에 오류를 띄우려고 noValidate를 쓴다 */}
      <form
        noValidate
        onSubmit={handleSubmit}
        style={{
          marginTop: 36,
          padding: "30px 32px 32px",
          border: "1px solid #ccc",
        }}
      >
        {errors.form && (
          <p role="alert" style={{ marginBottom: 22, fontSize: 13, color: "#c62828" }}>
            {errors.form}
          </p>
        )}
        <div style={{ display: "flex", flexDirection: "column", gap: 22 }}>
          {FIELDS.map((field) => {
            const error = errors[field.name];
            const errorId = `${field.name}-error`;
            return (
              <label
                key={field.name}
                style={{
                  display: "flex",
                  flexDirection: "column",
                  gap: 10,
                  fontSize: 15,
                  fontWeight: 700,
                }}
              >
                {field.label}
                <input
                  type={field.type}
                  name={field.name}
                  placeholder={field.placeholder}
                  autoComplete={field.autoComplete}
                  required
                  aria-invalid={error ? true : undefined}
                  aria-describedby={error ? errorId : undefined}
                  style={{
                    height: 52,
                    padding: "0 16px",
                    border: `1px solid ${error ? "#c62828" : "#ccc"}`,
                    borderRadius: 3,
                    fontSize: 14,
                    fontWeight: 400,
                  }}
                />
                {error && (
                  <span id={errorId} style={{ fontSize: 12, fontWeight: 400, color: "#c62828" }}>
                    {error}
                  </span>
                )}
              </label>
            );
          })}
        </div>

        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            marginTop: 26,
            paddingRight: 40,
            fontSize: 13,
          }}
        >
          <label style={{ display: "flex", alignItems: "center", gap: 10 }}>
            <input
              type="checkbox"
              name="remember"
              style={{ width: 16, height: 16 }}
            />
            로그인 상태 유지
          </label>
          <Link href="/forgot-password">비밀번호 찾기</Link>
        </div>

        <button
          type="submit"
          disabled={submitting}
          style={{
            width: "100%",
            height: 52,
            marginTop: 26,
            border: "none",
            background: "#1f1f1f",
            color: "#fff",
            fontSize: 15,
            fontWeight: 700,
            cursor: submitting ? "default" : "pointer",
            opacity: submitting ? 0.6 : 1,
          }}
        >
          {submitting ? "로그인 중…" : "로그인"}
        </button>
      </form>

      <p
        style={{
          marginTop: 60,
          fontSize: 14,
          color: "#555",
          textAlign: "center",
        }}
      >
        아직 계정이 없나요?{" "}
        <Link href="/signup" style={{ marginLeft: 4 }}>
          회원가입
        </Link>
      </p>
    </main>
  );
}
