// 로그아웃. refresh_token 쿠키를 백엔드에 본문으로 보내 이 기기의 리프레시 토큰을 폐기하고, 두 쿠키를 지운다(204).
// 백엔드에 닿지 못하거나 실패해도 쿠키는 지운다. 브라우저에서는 바로 로그아웃된 상태가 된다.
// 이미 발급된 액세스 토큰은 만료(30분)까지 유효하지만, 쿠키가 지워져 브라우저는 더 보내지 않는다.
import { NextResponse, type NextRequest } from "next/server";
import { BACKEND_URL, clearAuthCookies, crossSiteForbidden, isCrossSiteWrite, REFRESH_TOKEN_COOKIE } from "@/lib/api/bff";

export async function POST(request: NextRequest) {
  if (isCrossSiteWrite(request)) {
    return crossSiteForbidden();
  }
  const refreshToken = request.cookies.get(REFRESH_TOKEN_COOKIE)?.value;
  if (refreshToken) {
    try {
      await fetch(`${BACKEND_URL}/api/auth/logout`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ refreshToken }),
        cache: "no-store",
      });
    } catch {
      // 백엔드에 닿지 못해도 로그아웃은 진행한다. 남은 리프레시 토큰은 만료되면 Redis에서 사라진다
    }
  }
  const response = new NextResponse(null, { status: 204 });
  clearAuthCookies(response);
  return response;
}
