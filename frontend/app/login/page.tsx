import Link from "next/link";

// 와이어프레임: 로그인 (login)
// 오류 상태: 필수값 또는 이메일 형식 오류는 해당 입력 필드 아래에 표시한다.

const FIELDS = [
  {
    name: "email",
    type: "email",
    label: "이메일",
    placeholder: "이메일 주소를 입력하세요",
  },
  {
    name: "password",
    type: "password",
    label: "비밀번호",
    placeholder: "비밀번호를 입력하세요",
  },
];

export default function LoginPage() {
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

      {/* TODO: 로그인 요청 연결 */}
      <form
        style={{
          marginTop: 36,
          padding: "30px 32px 32px",
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
                required
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
          style={{
            width: "100%",
            height: 52,
            marginTop: 26,
            border: "none",
            background: "#1f1f1f",
            color: "#fff",
            fontSize: 15,
            fontWeight: 700,
            cursor: "pointer",
          }}
        >
          로그인
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
