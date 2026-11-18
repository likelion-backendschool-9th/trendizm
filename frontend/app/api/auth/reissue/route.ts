// 토큰 재발급. refresh_token 쿠키를 백엔드에 본문으로 보내고, 성공하면 두 쿠키를 새 토큰으로 바꾼다.
// apiFetch가 401을 받으면 자동으로 부른다.
// - 403(이용 제한): 두 쿠키를 지워 로그인이 풀린 상태로 만든다.
// - 401(토큰 만료·이미 사용됨): 쿠키를 지우지 않는다. 탭 여러 개가 동시에 재발급하면 늦은 탭은 이미 쓴 토큰이라 401을 받는데,
//   이때 쿠키를 지우면 먼저 성공한 탭이 저장한 새 쿠키까지 지워져 모든 탭이 로그아웃되기 때문이다.
//   남은 쿠키가 정말 만료된 것이면 다음 요청도 401이 되어 apiFetch가 로그인 화면으로 보낸다.
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
  return forwardLogin("/api/auth/reissue", JSON.stringify({ refreshToken }), { clearCookiesOnStatus: [403] });
}
