// 나머지 모든 /api/** 요청을 백엔드로 전달한다(BFF).
// - access_token 쿠키가 있으면 Authorization: Bearer 헤더로 바꿔 붙인다. 쿠키 자체는 백엔드에 보내지 않는다.
// - 로그인·가입·재발급은 app/api/auth/*/route.ts가 따로 처리한다(이 파일보다 먼저 맞는다).
// - 응답은 상태 코드와 본문을 그대로 돌려준다. 백엔드에 닿지 못하면 502 NETWORK_ERROR.
import type { NextRequest } from "next/server";
import { ACCESS_TOKEN_COOKIE, BACKEND_URL, backendUnavailable } from "@/lib/api/bff";

/** 브라우저 요청에서 백엔드로 넘길 헤더. 쿠키·Authorization 등 나머지는 넘기지 않는다 */
const FORWARD_REQUEST_HEADERS = ["accept", "accept-language", "content-type"];
/** 백엔드 응답에서 브라우저로 넘길 헤더 */
const FORWARD_RESPONSE_HEADERS = ["content-type", "content-disposition", "cache-control"];

async function forward(request: NextRequest, ctx: RouteContext<"/api/[...path]">) {
  const { path } = await ctx.params;
  const url = `${BACKEND_URL}/api/${path.map(encodeURIComponent).join("/")}${request.nextUrl.search}`;

  const headers = new Headers();
  for (const name of FORWARD_REQUEST_HEADERS) {
    const value = request.headers.get(name);
    if (value) {
      headers.set(name, value);
    }
  }
  const accessToken = request.cookies.get(ACCESS_TOKEN_COOKIE)?.value;
  if (accessToken) {
    headers.set("authorization", `Bearer ${accessToken}`);
  }

  const hasBody = request.method !== "GET" && request.method !== "HEAD";
  let backendResponse: Response;
  try {
    backendResponse = await fetch(url, {
      method: request.method,
      headers,
      body: hasBody ? await request.arrayBuffer() : undefined,
      redirect: "manual",
      cache: "no-store",
    });
  } catch {
    return backendUnavailable();
  }

  const responseHeaders = new Headers();
  for (const name of FORWARD_RESPONSE_HEADERS) {
    const value = backendResponse.headers.get(name);
    if (value) {
      responseHeaders.set(name, value);
    }
  }
  return new Response(backendResponse.body, { status: backendResponse.status, headers: responseHeaders });
}

export const GET = forward;
export const POST = forward;
export const PUT = forward;
export const PATCH = forward;
export const DELETE = forward;
