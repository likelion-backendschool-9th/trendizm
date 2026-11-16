// 와이어프레임: 개인정보 설정 (mypage/privacy)

const PROFILE = {
  nickname: "홍길동",
  email: "user@example.com",
};

const NOTIFICATIONS = [
  {
    name: "service",
    title: "서비스 알림",
    description: "서비스 이용에 필요한 안내를 받습니다.",
    enabled: true,
  },
  {
    name: "marketing",
    title: "마케팅 정보",
    description: "이벤트 및 혜택 정보를 받습니다.",
    enabled: false,
  },
];

const PASSWORD_FIELDS = [
  { name: "currentPassword", label: "현재 비밀번호", placeholder: "현재 비밀번호 입력" },
  { name: "newPassword", label: "새 비밀번호", placeholder: "새 비밀번호 입력" },
  {
    name: "newPasswordConfirm",
    label: "새 비밀번호 확인",
    placeholder: "새 비밀번호 다시 입력",
  },
];

export default function PrivacyPage() {
  return (
    <main style={{ display: "flex", flexDirection: "column", gap: 24 }}>
      <header style={{ marginBottom: 12 }}>
        <h1 style={{ fontSize: 26, fontWeight: 700 }}>개인정보 설정</h1>
        <p style={{ marginTop: 14, fontSize: 13, color: "#555" }}>
          계정 정보를 확인하고 보안 설정을 변경할 수 있습니다.
        </p>
      </header>

      <section style={{ padding: "22px 22px 20px", border: "1px solid #999" }}>
        <h2 style={{ fontSize: 20, fontWeight: 700 }}>기본 정보</h2>
        {/* TODO: 기본 정보 저장 요청 연결 */}
        <form style={{ display: "flex", flexDirection: "column", gap: 18, marginTop: 14 }}>
          {[
            { name: "nickname", type: "text", label: "닉네임", value: PROFILE.nickname },
            { name: "email", type: "email", label: "이메일", value: PROFILE.email },
          ].map((field) => (
            <label
              key={field.name}
              style={{ display: "flex", flexDirection: "column", gap: 8, fontSize: 14 }}
            >
              {field.label}
              <input
                type={field.type}
                name={field.name}
                defaultValue={field.value}
                style={{
                  height: 42,
                  padding: "0 14px",
                  border: "1px solid #999",
                  fontSize: 13,
                }}
              />
            </label>
          ))}
          <p style={{ fontSize: 12, color: "#555" }}>
            이메일 변경 시 본인 확인이 필요합니다.
          </p>
          <button
            type="submit"
            style={{
              width: 170,
              height: 42,
              padding: "0 12px",
              border: "none",
              background: "#1b1b1b",
              color: "#fff",
              fontSize: 13,
              fontWeight: 700,
              textAlign: "left",
              cursor: "pointer",
            }}
          >
            변경사항 저장
          </button>
        </form>
      </section>

      <section style={{ padding: "22px 22px 28px", border: "1px solid #999" }}>
        <h2 style={{ fontSize: 20, fontWeight: 700 }}>비밀번호 변경</h2>
        {/* TODO: 비밀번호 변경 요청 연결 */}
        <form style={{ display: "flex", flexDirection: "column", gap: 18, marginTop: 14 }}>
          {PASSWORD_FIELDS.map((field) => (
            <label
              key={field.name}
              style={{ display: "flex", flexDirection: "column", gap: 8, fontSize: 14 }}
            >
              {field.label}
              <input
                type="password"
                name={field.name}
                placeholder={field.placeholder}
                style={{
                  height: 42,
                  padding: "0 14px",
                  border: "1px solid #999",
                  background: "#f2f2f2",
                  fontSize: 13,
                }}
              />
            </label>
          ))}
          <p style={{ fontSize: 12, color: "#555" }}>
            영문·숫자·특수문자를 조합해 8자 이상 입력하세요.
          </p>
          <button
            type="submit"
            style={{
              width: 170,
              height: 42,
              padding: "0 12px",
              border: "none",
              background: "#1b1b1b",
              color: "#fff",
              fontSize: 13,
              fontWeight: 700,
              textAlign: "left",
              cursor: "pointer",
            }}
          >
            비밀번호 변경
          </button>
        </form>
      </section>

      <section style={{ padding: "22px 22px 20px", border: "1px solid #999" }}>
        <h2 style={{ fontSize: 20, fontWeight: 700 }}>수신 및 동의 설정</h2>
        <ul
          style={{
            display: "flex",
            flexDirection: "column",
            gap: 12,
            marginTop: 14,
            listStyle: "none",
          }}
        >
          {NOTIFICATIONS.map((notification) => (
            <li
              key={notification.name}
              style={{
                display: "flex",
                alignItems: "center",
                justifyContent: "space-between",
                padding: "6px 26px 8px 16px",
                border: "1px solid #999",
              }}
            >
              <div>
                <p style={{ fontSize: 14 }}>{notification.title}</p>
                <p style={{ marginTop: 4, fontSize: 11, color: "#555" }}>
                  {notification.description}
                </p>
              </div>
              {/* TODO: 수신 설정 변경 요청 연결 */}
              <button
                type="button"
                aria-pressed={notification.enabled}
                style={{
                  width: 86,
                  height: 28,
                  padding: "0 12px",
                  border: "1px solid #999",
                  background: notification.enabled ? "#f2f2f2" : "#fff",
                  fontSize: 12,
                  textAlign: "left",
                  cursor: "pointer",
                }}
              >
                {notification.enabled ? "켜짐" : "꺼짐"}
              </button>
            </li>
          ))}
        </ul>
        {/* TODO: 개인정보 처리방침 화면이 생기면 링크 연결 */}
        <p style={{ marginTop: 12, fontSize: 12, color: "#555" }}>
          개인정보 처리방침 보기 →
        </p>
      </section>

      <section style={{ padding: "22px 22px 12px", border: "1px solid #999" }}>
        <h2 style={{ fontSize: 20, fontWeight: 700 }}>계정 관리</h2>
        {/* TODO: 회원 탈퇴 흐름 연결 */}
        <button
          type="button"
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            width: "100%",
            height: 58,
            marginTop: 14,
            padding: "0 26px 0 16px",
            border: "1px solid #999",
            background: "#fff",
            fontSize: 14,
            cursor: "pointer",
          }}
        >
          회원 탈퇴
          <span style={{ fontSize: 12, color: "#555" }}>계정 및 데이터 삭제 →</span>
        </button>
      </section>
    </main>
  );
}
