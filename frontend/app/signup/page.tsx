"use client";

import Link from "next/link";
import { useState, type FocusEvent, type FormEvent } from "react";
import { apiFetch } from "@/lib/api/client";
import { ApiError, type LoginMember } from "@/lib/api/types";

// 와이어프레임: 회원가입
// 오류는 해당 입력칸 아래에, 입력칸과 관계없는 오류는 폼 위에 표시한다.
// 가입하면 바로 로그인 상태가 되고 추가 정보 입력(/signup/further)으로 간다.

type FieldName = "email" | "nickname" | "password" | "passwordConfirm";
type TermName = "agreeTerms" | "agreePrivacy" | "agreeMarketing";
type Errors = Partial<Record<FieldName | TermName | "form", string>>;
type Values = Record<FieldName, string>;

const FIELDS: { name: FieldName; type: string; label: string; placeholder: string; autoComplete: string }[] = [
  {
    name: "email",
    type: "email",
    label: "이메일",
    placeholder: "이메일 주소를 입력하세요",
    autoComplete: "email",
  },
  {
    name: "nickname",
    type: "text",
    label: "닉네임",
    placeholder: "닉네임을 입력하세요",
    autoComplete: "nickname",
  },
  {
    name: "password",
    type: "password",
    label: "비밀번호",
    placeholder: "8자 이상 입력하세요",
    autoComplete: "new-password",
  },
  {
    name: "passwordConfirm",
    type: "password",
    label: "비밀번호 확인",
    placeholder: "비밀번호를 다시 입력하세요",
    autoComplete: "new-password",
  },
];

const TERMS: { name: TermName; label: string; required: boolean }[] = [
  { name: "agreeTerms", label: "[필수] 이용약관 동의", required: true },
  {
    name: "agreePrivacy",
    label: "[필수] 개인정보 처리방침 동의",
    required: true,
  },
  {
    name: "agreeMarketing",
    label: "[선택] 마케팅 정보 수신 동의",
    required: false,
  },
];

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

/** 가입 응답의 중복 오류 코드 → 표시할 입력칸. 백엔드는 이 오류에 필드 정보(errors)를 주지 않는다 */
const DUPLICATE_FIELD: Record<string, FieldName> = {
  DUPLICATE_EMAIL: "email",
  DUPLICATE_NICKNAME: "nickname",
};

/** 입력칸 하나를 검사한다. 규칙과 메시지는 백엔드 SignupRequest와 같게 둔다. 문제없으면 undefined */
function validateField(name: FieldName, values: Values): string | undefined {
  const value = values[name];
  switch (name) {
    case "email":
      if (!value) return "이메일을 입력해 주세요.";
      if (!EMAIL_PATTERN.test(value)) return "이메일 형식이 아닙니다.";
      if (value.length > 100) return "이메일은 100자 이하여야 합니다.";
      return undefined;
    case "nickname":
      if (!value) return "닉네임을 입력해 주세요.";
      if (/\s/.test(value)) return "닉네임에는 공백을 넣을 수 없습니다.";
      if (value.length < 2 || value.length > 30) return "닉네임은 2자 이상 30자 이하여야 합니다.";
      return undefined;
    case "password":
      if (!value) return "비밀번호를 입력해 주세요.";
      if (value.length < 8 || value.length > 64) return "비밀번호는 8자 이상 64자 이하여야 합니다.";
      return undefined;
    case "passwordConfirm":
      if (!value) return "비밀번호를 한 번 더 입력해 주세요.";
      if (value !== values.password) return "비밀번호가 일치하지 않습니다.";
      return undefined;
  }
}

function readValues(form: HTMLFormElement): Values {
  const data = new FormData(form);
  const read = (name: FieldName) => String(data.get(name) ?? "");
  return {
    email: read("email").trim(),
    nickname: read("nickname"),
    password: read("password"),
    passwordConfirm: read("passwordConfirm"),
  };
}

export default function SignupPage() {
  const [errors, setErrors] = useState<Errors>({});
  const [agreed, setAgreed] = useState<Record<TermName, boolean>>({
    agreeTerms: false,
    agreePrivacy: false,
    agreeMarketing: false,
  });
  const [submitting, setSubmitting] = useState(false);

  const allAgreed = TERMS.every((term) => agreed[term.name]);

  function setFieldError(name: keyof Errors, message: string | undefined) {
    setErrors((prev) => ({ ...prev, [name]: message }));
  }

  /** 입력칸을 벗어나면 그 칸을 검사하고, 이메일·닉네임은 이미 쓰이는지 확인한다 */
  async function handleBlur(event: FocusEvent<HTMLInputElement>) {
    const name = event.target.name as FieldName;
    const values = readValues(event.target.form!);
    // 아직 아무것도 입력하지 않은 칸은 지나가기만 해도 오류를 띄우지 않는다
    if (!values[name]) {
      return;
    }
    const message = validateField(name, values);
    setFieldError(name, message);
    if (name === "password" && values.passwordConfirm) {
      setFieldError("passwordConfirm", validateField("passwordConfirm", values));
    }
    if (message || (name !== "email" && name !== "nickname")) {
      return;
    }

    const query = `${name}=${encodeURIComponent(values[name])}`;
    try {
      const { available } = await apiFetch<{ available: boolean }>(`/api/auth/check-${name}?${query}`);
      if (!available) {
        setFieldError(
          name,
          name === "email" ? "이미 가입했거나 탈퇴 후 30일이 지나지 않은 이메일입니다." : "이미 사용 중인 닉네임입니다.",
        );
      }
    } catch (error) {
      // 중복 확인을 못 해도 가입할 때 서버가 다시 확인하므로 넘어간다. 형식 오류만 보여 준다
      if (error instanceof ApiError) {
        const fieldMessage = error.fieldMessage(name);
        if (fieldMessage) {
          setFieldError(name, fieldMessage);
        }
      }
    }
  }

  function toggleAll(checked: boolean) {
    setAgreed({ agreeTerms: checked, agreePrivacy: checked, agreeMarketing: checked });
    if (checked) {
      setErrors((prev) => ({ ...prev, agreeTerms: undefined, agreePrivacy: undefined }));
    }
  }

  function toggleTerm(name: TermName, checked: boolean) {
    setAgreed((prev) => ({ ...prev, [name]: checked }));
    if (checked) {
      setFieldError(name, undefined);
    }
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (submitting) {
      return;
    }
    const values = readValues(event.currentTarget);

    const found: Errors = {};
    for (const field of FIELDS) {
      found[field.name] = validateField(field.name, values);
    }
    if (!agreed.agreeTerms) found.agreeTerms = "이용약관에 동의해 주세요.";
    if (!agreed.agreePrivacy) found.agreePrivacy = "개인정보 처리방침에 동의해 주세요.";
    setErrors(found);
    if (Object.values(found).some(Boolean)) {
      return;
    }

    setSubmitting(true);
    try {
      await apiFetch<LoginMember>("/api/auth/signup", {
        method: "POST",
        body: {
          email: values.email,
          password: values.password,
          nickname: values.nickname,
          agreeTerms: agreed.agreeTerms,
          agreePrivacy: agreed.agreePrivacy,
          marketingAgreed: agreed.agreeMarketing,
        },
      });
      // 가입하면 바로 로그인 상태다. 헤더도 로그인 상태로 다시 그려지도록 전체 이동한다
      // eslint-disable-next-line @next/next/no-location-assign-relative-destination
      window.location.assign("/signup/further");
    } catch (error) {
      setSubmitting(false);
      if (!(error instanceof ApiError)) {
        setErrors({ form: "요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요." });
        return;
      }
      const duplicateField = DUPLICATE_FIELD[error.code];
      if (duplicateField) {
        setErrors({ [duplicateField]: error.message });
        return;
      }
      if (error.errors.length > 0) {
        const fromServer: Errors = {};
        for (const fieldError of error.errors) {
          // 백엔드는 마케팅 동의를 marketingAgreed로 받는다
          const name = fieldError.field === "marketingAgreed" ? "agreeMarketing" : fieldError.field;
          fromServer[name as keyof Errors] = fieldError.message;
        }
        setErrors(fromServer);
        return;
      }
      setErrors({ form: error.message });
    }
  }

  /** 입력칸을 고치기 시작하면 그 칸의 오류를 지운다. 칸을 벗어날 때 다시 검사한다 */
  function clearError(name: FieldName) {
    if (errors[name]) {
      setFieldError(name, undefined);
    }
  }

  return (
    <main
      style={{
        width: "100%",
        maxWidth: 552,
        margin: "0 auto",
        padding: "36px 16px 64px",
        background: "#fff",
        color: "#111",
      }}
    >
      <nav aria-label="경로" style={{ fontSize: 14, color: "#555" }}>
        <Link href="/">홈</Link>
        <span style={{ margin: "0 8px" }}>/</span>
        회원가입
      </nav>
      <h1
        style={{
          position: "absolute",
          width: 1,
          height: 1,
          overflow: "hidden",
          clip: "rect(0 0 0 0)",
          whiteSpace: "nowrap",
        }}
      >
        회원가입
      </h1>
      <p style={{ marginTop: 18, fontSize: 14, color: "#555" }}>
        계정을 만들고 관심 있는 트렌드를 저장해 보세요.
      </p>

      {/* 브라우저 기본 말풍선 대신 입력칸 아래에 오류를 띄우려고 noValidate를 쓴다 */}
      <form
        noValidate
        onSubmit={handleSubmit}
        style={{
          marginTop: 38,
          padding: "30px 32px 14px",
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
                  onBlur={handleBlur}
                  onChange={() => clearError(field.name)}
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
        <p style={{ marginTop: 24, fontSize: 12, color: "#666" }}>
          * 비밀번호는 8자 이상으로 설정해 주세요.
        </p>

        <hr
          style={{
            margin: "34px 0 18px",
            border: "none",
            borderTop: "1px solid #aaa",
          }}
        />

        <div style={{ display: "flex", flexDirection: "column", gap: 34 }}>
          <label
            style={{
              display: "flex",
              alignItems: "center",
              gap: 12,
              fontSize: 13,
            }}
          >
            <input
              type="checkbox"
              name="agreeAll"
              checked={allAgreed}
              onChange={(event) => toggleAll(event.target.checked)}
              style={{ width: 20, height: 20 }}
            />
            전체 약관에 동의합니다
          </label>
          {TERMS.map((term) => {
            const error = errors[term.name];
            return (
              <div key={term.name}>
                <div style={{ display: "flex", alignItems: "center" }}>
                  <label
                    style={{
                      display: "flex",
                      alignItems: "center",
                      gap: 12,
                      flex: 1,
                      fontSize: 13,
                    }}
                  >
                    <input
                      type="checkbox"
                      name={term.name}
                      required={term.required}
                      checked={agreed[term.name]}
                      onChange={(event) => toggleTerm(term.name, event.target.checked)}
                      aria-invalid={error ? true : undefined}
                      aria-describedby={error ? `${term.name}-error` : undefined}
                      style={{ width: 20, height: 20 }}
                    />
                    {term.label}
                  </label>
                  {/* TODO: 약관 내용 보기 연결 */}
                  <button
                    type="button"
                    style={{
                      width: 86,
                      border: "none",
                      background: "none",
                      color: "#555",
                      fontSize: 12,
                      textAlign: "left",
                      cursor: "pointer",
                    }}
                  >
                    보기
                  </button>
                </div>
                {error && (
                  <p id={`${term.name}-error`} style={{ marginTop: 8, fontSize: 12, color: "#c62828" }}>
                    {error}
                  </p>
                )}
              </div>
            );
          })}
        </div>

        <button
          type="submit"
          disabled={submitting}
          style={{
            width: "100%",
            height: 52,
            marginTop: 34,
            border: "none",
            background: "#1b1b1b",
            color: "#fff",
            fontSize: 16,
            fontWeight: 700,
            cursor: submitting ? "default" : "pointer",
            opacity: submitting ? 0.6 : 1,
          }}
        >
          {submitting ? "가입 중…" : "가입하기"}
        </button>
      </form>

      <p
        style={{
          marginTop: 36,
          textAlign: "center",
          fontSize: 14,
          color: "#555",
        }}
      >
        이미 계정이 있나요?{" "}
        <Link href="/login" style={{ marginLeft: 4 }}>
          로그인
        </Link>
      </p>
    </main>
  );
}
