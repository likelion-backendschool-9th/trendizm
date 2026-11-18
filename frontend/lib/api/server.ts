// 서버 컴포넌트에서 백엔드 API를 부를 때 쓰는 함수. 클라이언트 컴포넌트에서는 client.ts의 apiFetch를 쓴다.
// - 서버에서는 Next.js 프록시를 거치지 않고 백엔드로 바로 요청한다.
// - withAuth: true면 브라우저가 보낸 로그인 쿠키를 백엔드에 그대로 전달한다.
//   쿠키를 읽으면 그 페이지는 미리 만들어 둘 수 없으므로(Cache Components) 로그인이 필요할 때만 켠다.
//   이때 서버 컴포넌트는 쿠키를 새로 쓸 수 없어서 토큰 재발급은 하지 않는다. 401이면 ApiError(401)를 던진다.
// - 실패하면 ApiError를 던진다.

import { cookies } from "next/headers";
import { ApiError, readErrorResponse } from "./types";

const backendUrl = process.env.BACKEND_URL ?? "http://localhost:8080";

export type ServerFetchOptions = Omit<RequestInit, "body"> & {
  body?: unknown;
  /** 로그인 쿠키를 전달할지. 기본 false (공개 API) */
  withAuth?: boolean;
};

export async function serverFetch<T = void>(path: string, options: ServerFetchOptions = {}): Promise<T> {
  const { body, withAuth = false, headers, ...init } = options;

  const cookieHeader = withAuth ? (await cookies()).toString() : undefined;

  const response = await fetch(`${backendUrl}${path}`, {
    ...init,
    headers: {
      Accept: "application/json",
      ...(body !== undefined ? { "Content-Type": "application/json" } : {}),
      ...(cookieHeader ? { Cookie: cookieHeader } : {}),
      ...headers,
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  });

  if (!response.ok) {
    throw new ApiError(response.status, await readErrorResponse(response));
  }
  if (response.status === 204) {
    return undefined as T;
  }
  const text = await response.text();
  return (text ? JSON.parse(text) : undefined) as T;
}
