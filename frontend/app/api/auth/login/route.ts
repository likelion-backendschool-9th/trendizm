// 로그인. 본문 { email, password, rememberMe }를 백엔드에 보내고, 성공하면 토큰을 쿠키로 저장한 뒤 회원 정보(LoginMember)를 준다.
import type { NextRequest } from "next/server";
import { forwardLogin } from "@/lib/api/bff";

export async function POST(request: NextRequest) {
  return forwardLogin("/api/auth/login", await request.text());
}
