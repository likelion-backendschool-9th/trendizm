import Link from "next/link";
import QuestionForm from "@/app/item/new/_components/QuestionForm";

// 트렌드 아이템 질문하기. 로그인하지 않았으면 폼이 로그인 화면으로 보낸다.
// /item/[id]보다 이 고정 경로가 먼저 맞는다.
export default function NewItemQuestionPage() {
  return (
    <main
      style={{
        width: "100%",
        maxWidth: 800,
        margin: "0 auto",
        padding: "24px 32px 48px",
        background: "#fff",
        color: "#111",
      }}
    >
      <nav aria-label="경로" style={{ fontSize: 13, color: "#777" }}>
        <Link href="/item">‹ 트렌드 아이템</Link>
      </nav>
      <h1 style={{ marginTop: 16, fontSize: 26, fontWeight: 700 }}>질문하기</h1>
      <p style={{ marginTop: 6, fontSize: 13, color: "#777" }}>
        찾는 아이템을 자세히 적을수록 딱 맞는 추천을 받을 수 있어요
      </p>
      <QuestionForm />
    </main>
  );
}
