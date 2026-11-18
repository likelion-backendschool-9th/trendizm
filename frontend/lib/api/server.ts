// 서버 컴포넌트에서 백엔드 API를 부를 때 쓰는 함수. 클라이언트 컴포넌트에서는 client.ts의 apiFetch를 쓴다.
// - 서버에서는 app/api(BFF)를 거치지 않고 백엔드로 바로 요청한다.
// - withAuth: true면 브라우저가 보낸 access_token 쿠키를 Authorization: Bearer 헤더로 바꿔 백엔드에 보낸다.
//   쿠키를 읽으면 그 페이지는 미리 만들어 둘 수 없으므로(Cache Components) 로그인이 필요할 때만 켠다.
//   이때 서버 컴포넌트는 쿠키를 새로 쓸 수 없어서 토큰 재발급은 하지 않는다. 401이면 ApiError(401)를 던진다.
// - 실패하면 ApiError를 던진다. 서버에 닿지 못하면 ApiError(status 0, code "NETWORK_ERROR")다.
//
// ⚠️ next.config.ts의 cacheComponents가 켜져 있어서, 페이지를 만들 때 이 함수를 그냥 부르면 빌드가 실패한다
//    ("Next.js encountered uncached or runtime data during prerendering").
//    - 이 함수를 부르는 컴포넌트를 <Suspense>로 감싼다. 공개 데이터든 로그인이 필요한 데이터든 지금은 모두 이렇게 한다.
//      예: <Suspense fallback={<Loading />}><MyPoints /></Suspense>  (MyPoints 안에서 serverFetch(..., { withAuth: true }))
//    - "use cache"는 쓰지 않는다. "use cache"를 붙인 데이터는 빌드할 때 미리 만들어지므로 빌드 중에 백엔드를 부르는데,
//      CI·배포 빌드에는 백엔드가 없어서 빌드가 실패한다. 빌드 환경에서 백엔드에 접근할 수 있게 되면 그때 쓴다.

import { cookies } from "next/headers";
import { ACCESS_TOKEN_COOKIE, BACKEND_URL } from "./bff";
import { ApiError, fetchOrNetworkError, readErrorResponse } from "./types";

export type ServerFetchOptions = Omit<RequestInit, "body"> & {
  body?: unknown;
  /** 로그인 토큰(쿠키)을 Authorization 헤더로 보낼지. 기본 false (공개 API) */
  withAuth?: boolean;
};

export async function serverFetch<T = void>(path: string, options: ServerFetchOptions = {}): Promise<T> {
  const { body, withAuth = false, headers, ...init } = options;

  const accessToken = withAuth ? (await cookies()).get(ACCESS_TOKEN_COOKIE)?.value : undefined;

  const response = await fetchOrNetworkError(`${BACKEND_URL}${path}`, {
    ...init,
    headers: {
      Accept: "application/json",
      ...(body !== undefined ? { "Content-Type": "application/json" } : {}),
      ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
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
