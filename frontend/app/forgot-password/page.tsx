import Link from "next/link";

// 와이어프레임: 비밀번호 찾기 · 이메일 입력 (forgot-password)
// 필수값·이메일 형식 오류는 입력 필드 아래에 표시한다.

export default function ForgotPasswordPage() {
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
        <Link href="/">홈</Link> / <Link href="/login">로그인</Link> / 비밀번호
        찾기
      </nav>
      <h1 style={{ marginTop: 22, fontSize: 28, fontWeight: 700 }}>
        비밀번호 찾기
      </h1>
      <p style={{ marginTop: 14, fontSize: 14, color: "#555" }}>
        가입한 이메일로 비밀번호 재설정 링크를 보내드립니다.
      </p>

      {/* TODO: 재설정 링크 발송 요청 연결 후 /forgot-password/sent로 이동 */}
      <form
        style={{
          marginTop: 38,
          padding: "30px 32px 32px",
          border: "1px solid #ccc",
        }}
      >
        <label
          style={{
            display: "flex",
            flexDirection: "column",
            gap: 10,
            fontSize: 15,
            fontWeight: 700,
          }}
        >
          가입한 이메일
          <input
            type="email"
            name="email"
            placeholder="이메일 주소를 입력하세요"
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
        {/* 가입 여부를 알 수 없도록 계정이 있든 없든 같은 안내를 보여 준다. */}
        <p style={{ marginTop: 22, fontSize: 12, color: "#555" }}>
          계정 유무와 관계없이 동일한 발송 안내를 표시합니다.
        </p>

        <button
          type="submit"
          style={{
            width: "100%",
            height: 52,
            marginTop: 18,
            border: "none",
            background: "#1f1f1f",
            color: "#fff",
            fontSize: 15,
            fontWeight: 700,
            cursor: "pointer",
          }}
        >
          재설정 링크 보내기
        </button>
      </form>

      <p style={{ marginTop: 40, textAlign: "center" }}>
        <Link href="/login" style={{ fontSize: 14, color: "#555" }}>
          ← 로그인으로 돌아가기
        </Link>
      </p>
    </main>
  );
}
