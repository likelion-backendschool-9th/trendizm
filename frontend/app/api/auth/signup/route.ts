// 회원가입. 가입하면 바로 로그인 상태가 된다(201 + 회원 정보, 토큰은 쿠키로 저장).
import type { NextRequest } from "next/server";
import { crossSiteForbidden, forwardLogin, isCrossSiteWrite } from "@/lib/api/bff";

export async function POST(request: NextRequest) {
  if (isCrossSiteWrite(request)) {
    return crossSiteForbidden();
  }
  return forwardLogin("/api/auth/signup", await request.text());
}
