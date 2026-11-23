// 로그인한 회원 정보(헤더 표시용). 백엔드 GET /api/auth/me에 access_token 쿠키를 Bearer 헤더로 붙여 전달한다.
// - 쿠키가 하나도 없으면 백엔드를 부르지 않고 바로 401을 준다(비로그인 방문자가 페이지마다 백엔드를 부르지 않게).
// - 여기서는 재발급하지 않는다. 401이면 apiFetch가 /api/auth/reissue로 재발급하고 다시 부른다(탭 동시 재발급도 거기서 처리한다).
import { NextResponse, type NextRequest } from "next/server";
import { ACCESS_TOKEN_COOKIE, BACKEND_URL, backendUnavailable, REFRESH_TOKEN_COOKIE } from "@/lib/api/bff";

export async function GET(request: NextRequest) {
  const accessToken = request.cookies.get(ACCESS_TOKEN_COOKIE)?.value;
  if (!accessToken && !request.cookies.has(REFRESH_TOKEN_COOKIE)) {
    return NextResponse.json(
      { code: "UNAUTHORIZED", message: "로그인이 필요합니다." },
      { status: 401, headers: { "Cache-Control": "no-store" } },
    );
  }

  let backendResponse: Response;
  try {
    backendResponse = await fetch(`${BACKEND_URL}/api/auth/me`, {
      headers: {
        Accept: "application/json",
        ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
      },
      cache: "no-store",
    });
  } catch {
    return backendUnavailable();
  }
  return new NextResponse(backendResponse.body, {
    status: backendResponse.status,
    headers: {
      "Content-Type": backendResponse.headers.get("Content-Type") ?? "application/json",
      "Cache-Control": "no-store",
    },
  });
}
