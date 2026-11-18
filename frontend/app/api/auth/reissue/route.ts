// 토큰 재발급. refresh_token 쿠키를 백엔드에 본문으로 보내고, 성공하면 두 쿠키를 새 토큰으로 바꾼다.
// apiFetch가 401을 받으면 자동으로 부른다. 실패(401)하면 두 쿠키를 지워 로그인이 풀린 상태로 만든다.
import { NextResponse, type NextRequest } from "next/server";
import { clearAuthCookies, forwardLogin, REFRESH_TOKEN_COOKIE } from "@/lib/api/bff";

export async function POST(request: NextRequest) {
  const refreshToken = request.cookies.get(REFRESH_TOKEN_COOKIE)?.value;
  if (!refreshToken) {
    const response = NextResponse.json(
      { code: "INVALID_REFRESH_TOKEN", message: "로그인이 만료되었습니다. 다시 로그인해 주세요." },
      { status: 401 },
    );
    clearAuthCookies(response);
    return response;
  }
  return forwardLogin("/api/auth/reissue", JSON.stringify({ refreshToken }), { clearCookiesOnFailure: true });
}
