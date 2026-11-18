import Link from "next/link";

// 와이어프레임: 비밀번호 찾기 · 발송 안내 (forgot-password/sent)
// TODO: 링크 만료 시간과 재전송 정책은 인증 방식 확정 후 문구에 넣는다.

export default function ForgotPasswordSentPage() {
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
        <Link href="/">홈</Link> / <Link href="/login">로그인</Link> /{" "}
        <Link href="/forgot-password">비밀번호 찾기</Link>
      </nav>
      <h1 style={{ marginTop: 22, fontSize: 28, fontWeight: 700 }}>
        메일을 확인해 주세요
      </h1>
      <p style={{ marginTop: 14, fontSize: 14, color: "#555" }}>
        입력한 이메일 주소로 재설정 안내를 요청했습니다.
      </p>

      <section
        style={{
          marginTop: 38,
          padding: "26px 32px 12px",
          border: "1px solid #ccc",
        }}
      >
        <div
          aria-hidden
          style={{
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            width: 52,
            height: 52,
            margin: "0 auto",
            border: "1px solid #ccc",
            borderRadius: "50%",
            background: "#f4f4f4",
            fontSize: 26,
          }}
        >
          ✓
        </div>
        <h2 style={{ marginTop: 26, fontSize: 18, fontWeight: 700 }}>
          재설정 안내를 확인해 주세요
        </h2>
        <p style={{ marginTop: 20, fontSize: 12, lineHeight: 1.3, color: "#555" }}>
          메일이 보이지 않으면 스팸함도 확인해 주세요.
          <br />
          메일의 링크를 열어 새 비밀번호를 설정할 수 있습니다.
        </p>

        <Link
          href="/login"
          style={{
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            height: 52,
            marginTop: 26,
            background: "#1f1f1f",
            color: "#fff",
            fontSize: 15,
            fontWeight: 700,
          }}
        >
          로그인으로 돌아가기
        </Link>
      </section>

      <p style={{ marginTop: 32, textAlign: "center" }}>
        <Link href="/forgot-password" style={{ fontSize: 14, color: "#555" }}>
          다른 이메일 입력하기
        </Link>
      </p>
    </main>
  );
}
