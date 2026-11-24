// BFF(Next 서버) 전용 코드. app/api의 Route Handler와 server.ts에서만 import한다. 브라우저 코드에서 쓰지 않는다.
//
// 브라우저 ──/api/...(쿠키)──▶ Next 서버(app/api) ──Authorization: Bearer──▶ 백엔드(Spring)
// - 토큰은 HttpOnly 쿠키에만 둔다. 자바스크립트로 읽을 수 없고, 브라우저는 백엔드 주소를 모른다.
// - 로그인·가입·재발급은 백엔드가 토큰을 응답 본문으로 주고, 여기서 쿠키로 바꿔 저장한다. 브라우저에는 회원 정보만 준다.

import { NextResponse, type NextRequest } from "next/server";
import type { ErrorResponse, LoginMember } from "./types";

/** 백엔드 주소. 로컬은 기본값 그대로, 배포는 BACKEND_URL 환경변수로 바꾼다(서버에서 실행할 때 읽는다) */
export const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

export const ACCESS_TOKEN_COOKIE = "access_token";
export const REFRESH_TOKEN_COOKIE = "refresh_token";
/** refresh 쿠키는 로그인·재발급 경로에만 보낸다 */
const REFRESH_TOKEN_PATH = "/api/auth";

/** 백엔드의 로그인·가입·재발급 응답 */
export type LoginResponse = {
  member: LoginMember;
  accessToken: string;
  /** 남은 수명(초) */
  accessTokenExpiresIn: number;
  refreshToken: string;
  /** 남은 수명(초). rememberMe가 false면 쓰지 않는다 */
  refreshTokenExpiresIn: number;
  /** false면 refresh 쿠키를 브라우저를 닫으면 사라지게 저장한다 */
  rememberMe: boolean;
};

/** 백엔드에 닿지 못했을 때 브라우저에 주는 응답 (apiFetch에서 ApiError(502, "NETWORK_ERROR")가 된다) */
export function backendUnavailable(): NextResponse<ErrorResponse> {
  return NextResponse.json({ code: "NETWORK_ERROR", message: "서버에 연결할 수 없습니다." }, { status: 502 });
}

/**
 * 다른 사이트에서 보낸 쓰기 요청(POST·PUT·PATCH·DELETE)인지. 맞으면 처리하지 않는다(CSRF 방어).
 * - 로그인·가입은 쿠키 없이도 성공하는 요청이라 SameSite=Lax 쿠키로는 못 막는다. 다른 사이트의 숨은 폼이
 *   공격자 계정으로 로그인시키는 "로그인 CSRF"가 가능해서, 요청 출처(Origin)를 직접 확인한다.
 * - 브라우저는 다른 사이트에서 보내는 POST에 항상 Origin을 붙이고, 자바스크립트로 바꿀 수 없다.
 *   Origin이 없으면 브라우저가 아닌 클라이언트(curl 등)라서 통과시킨다. "null"(샌드박스 iframe 등)은 막는다.
 * - 우리 주소는 요청의 Host(배포에서는 nginx가 넘겨 주는 X-Forwarded-Host)로 판단한다.
 */
export function isCrossSiteWrite(request: NextRequest): boolean {
  if (request.method === "GET" || request.method === "HEAD" || request.method === "OPTIONS") {
    return false;
  }
  const origin = request.headers.get("origin");
  if (origin === null) {
    return false;
  }
  let originHost: string;
  try {
    originHost = new URL(origin).host;
  } catch {
    return true;
  }
  const hosts = [request.headers.get("x-forwarded-host"), request.headers.get("host")];
  return !hosts.some((host) => host !== null && host === originHost);
}

/** 다른 사이트에서 보낸 쓰기 요청에 주는 응답 */
export function crossSiteForbidden(): NextResponse<ErrorResponse> {
  return NextResponse.json(
    { code: "CROSS_SITE_REQUEST", message: "다른 사이트에서 보낸 요청은 처리하지 않습니다." },
    { status: 403 },
  );
}

/**
 * 쿠키에 Secure(https에서만 전송)를 붙일지. 기본: 운영 빌드(next start)면 붙이고 개발(next dev)이면 안 붙인다.
 * 배포가 아직 https가 아니면 COOKIE_SECURE=false로 끈다(Secure 쿠키는 http 사이트에 저장되지 않는다. localhost는 예외).
 */
const secure = process.env.COOKIE_SECURE ? process.env.COOKIE_SECURE === "true" : process.env.NODE_ENV === "production";

export function setAuthCookies(response: NextResponse, login: LoginResponse) {
  response.cookies.set(ACCESS_TOKEN_COOKIE, login.accessToken, {
    httpOnly: true,
    secure,
    sameSite: "lax",
    path: "/",
    maxAge: login.accessTokenExpiresIn,
  });
  response.cookies.set(REFRESH_TOKEN_COOKIE, login.refreshToken, {
    httpOnly: true,
    secure,
    sameSite: "lax",
    path: REFRESH_TOKEN_PATH,
    // 로그인 상태 유지를 안 했으면 Max-Age 없이 저장해서 브라우저를 닫으면 사라지게 한다
    ...(login.rememberMe ? { maxAge: login.refreshTokenExpiresIn } : {}),
  });
}

export function clearAuthCookies(response: NextResponse) {
  response.cookies.set(ACCESS_TOKEN_COOKIE, "", { httpOnly: true, secure, sameSite: "lax", path: "/", maxAge: 0 });
  response.cookies.set(REFRESH_TOKEN_COOKIE, "", {
    httpOnly: true,
    secure,
    sameSite: "lax",
    path: REFRESH_TOKEN_PATH,
    maxAge: 0,
  });
}

/**
 * 로그인·가입·재발급을 백엔드에 보내고, 성공하면 토큰을 쿠키로 저장한 뒤 회원 정보만 돌려준다.
 * 실패하면 백엔드의 상태 코드와 { code, message } 본문을 그대로 돌려준다. clearCookiesOnStatus에 있는 상태 코드면 두 쿠키를 지운다.
 */
export async function forwardLogin(
  backendPath: string,
  body: string,
  options: { clearCookiesOnStatus?: number[] } = {},
): Promise<NextResponse> {
  let backendResponse: Response;
  try {
    backendResponse = await fetch(`${BACKEND_URL}${backendPath}`, {
      method: "POST",
      headers: { "Content-Type": "application/json", Accept: "application/json" },
      body,
      cache: "no-store",
    });
  } catch {
    return backendUnavailable();
  }

  if (!backendResponse.ok) {
    const response = new NextResponse(await backendResponse.text(), {
      status: backendResponse.status,
      headers: { "Content-Type": backendResponse.headers.get("Content-Type") ?? "application/json" },
    });
    if (options.clearCookiesOnStatus?.includes(backendResponse.status)) {
      clearAuthCookies(response);
    }
    return response;
  }

  const login = (await backendResponse.json()) as LoginResponse;
  const response = NextResponse.json(login.member, { status: backendResponse.status });
  setAuthCookies(response, login);
  return response;
}
