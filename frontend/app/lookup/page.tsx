// 와이어프레임: 밈 검색 - 입력
export default function LookupPage() {
  return (
    <main
      style={{
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        width: "100%",
        minHeight: "100vh",
        padding: "48px 16px",
        color: "#222",
      }}
    >
      <h1 style={{ fontSize: 28, fontWeight: 700, textAlign: "center" }}>
        확인이 필요한 밈을 검색하세요
      </h1>

      {/* TODO: 검색 요청 연결 후 /lookup/[slug]로 이동 */}
      <form
        style={{
          display: "flex",
          flexDirection: "column",
          gap: 24,
          width: "100%",
          maxWidth: 448,
          marginTop: 64,
        }}
      >
        <label
          style={{
            display: "flex",
            flexDirection: "column",
            gap: 10,
            fontSize: 13,
          }}
        >
          사이트 주소
          <input
            type="url"
            name="url"
            placeholder="피드·동영상·게시물의 링크를 붙여넣으세요"
            style={{
              height: 48,
              padding: "0 16px",
              border: "1px solid #ccc",
              borderRadius: 6,
              background: "#fff",
              fontSize: 13,
            }}
          />
        </label>
        <label
          style={{
            display: "flex",
            flexDirection: "column",
            gap: 10,
            fontSize: 13,
          }}
        >
          단어
          <input
            type="text"
            name="word"
            placeholder="궁금한 밈이나 단어를 입력하세요"
            style={{
              height: 48,
              padding: "0 16px",
              border: "1px solid #ccc",
              borderRadius: 6,
              background: "#fff",
              fontSize: 13,
            }}
          />
        </label>
        <button
          type="submit"
          style={{
            height: 48,
            marginTop: 4,
            border: "none",
            borderRadius: 6,
            background: "#333539",
            color: "#fff",
            fontSize: 15,
            cursor: "pointer",
          }}
        >
          단어 검색
        </button>
      </form>
    </main>
  );
}
