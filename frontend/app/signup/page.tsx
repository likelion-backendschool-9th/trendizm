import Link from "next/link";

// 와이어프레임: 회원가입

const FIELDS = [
  {
    name: "email",
    type: "email",
    label: "이메일",
    placeholder: "이메일 주소를 입력하세요",
  },
  {
    name: "nickname",
    type: "text",
    label: "닉네임",
    placeholder: "닉네임을 입력하세요",
  },
  {
    name: "password",
    type: "password",
    label: "비밀번호",
    placeholder: "영문·숫자 조합 8자 이상",
  },
  {
    name: "passwordConfirm",
    type: "password",
    label: "비밀번호 확인",
    placeholder: "비밀번호를 다시 입력하세요",
  },
];

const TERMS = [
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

export default function SignupPage() {
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

      {/* TODO: 가입 요청 연결 */}
      <form
        style={{
          marginTop: 38,
          padding: "30px 32px 14px",
          border: "1px solid #ccc",
        }}
      >
        <div style={{ display: "flex", flexDirection: "column", gap: 22 }}>
          {FIELDS.map((field) => (
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
                style={{
                  height: 52,
                  padding: "0 16px",
                  border: "1px solid #ccc",
                  borderRadius: 3,
                  fontSize: 14,
                  fontWeight: 400,
                }}
              />
            </label>
          ))}
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

        {/* TODO: 전체 동의를 누르면 아래 항목을 함께 체크 */}
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
              style={{ width: 20, height: 20 }}
            />
            전체 약관에 동의합니다
          </label>
          {TERMS.map((term) => (
            <div
              key={term.name}
              style={{ display: "flex", alignItems: "center" }}
            >
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
          ))}
        </div>

        <button
          type="submit"
          style={{
            width: "100%",
            height: 52,
            marginTop: 34,
            border: "none",
            background: "#1b1b1b",
            color: "#fff",
            fontSize: 16,
            fontWeight: 700,
            cursor: "pointer",
          }}
        >
          가입하기
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
