"use client";

// 브라우저(클라이언트 컴포넌트)에서 백엔드 API를 부를 때 쓰는 함수.
// - 주소는 백엔드 경로 그대로 쓴다. 예: apiFetch("/api/auth/login", { method: "POST", body: {...} })
//   next.config.ts의 rewrites가 /api/** 를 백엔드로 전달한다.
// - 로그인 토큰은 HttpOnly 쿠키라서 자바스크립트로 다루지 않는다. 브라우저가 알아서 붙인다.
// - 401(로그인 만료)이면 /api/auth/reissue로 토큰을 한 번 새로 받고 원래 요청을 다시 보낸다.
//   그래도 401이면 로그인 화면으로 보낸다(redirectOnUnauthorized: false로 끌 수 있다).
// - 실패하면 ApiError를 던진다. code, message, errors(필드별 오류)가 들어 있다.

import { ApiError, readErrorResponse } from "./types";

export type ApiFetchOptions = Omit<RequestInit, "body"> & {
  /** 객체를 넘기면 JSON으로 보낸다. FormData는 그대로 보낸다 */
  body?: unknown;
  /** 재발급 후에도 401이면 로그인 화면으로 이동할지. 기본 true */
  redirectOnUnauthorized?: boolean;
};

const LOGIN_PATH = "/login";

/** 동시에 여러 요청이 401을 받아도 재발급은 한 번만 한다 */
let reissuing: Promise<boolean> | null = null;

function reissueOnce(): Promise<boolean> {
  if (!reissuing) {
    reissuing = fetch("/api/auth/reissue", { method: "POST", credentials: "same-origin" })
      .then((response) => response.ok)
      .catch(() => false)
      .finally(() => {
        reissuing = null;
      });
  }
  return reissuing;
}

/** 로그인·가입·재발급 요청은 401을 받아도 재발급을 시도하지 않는다 */
function isAuthRequest(path: string): boolean {
  return path.startsWith("/api/auth/");
}

function goToLogin() {
  if (typeof window === "undefined" || window.location.pathname === LOGIN_PATH) {
    return;
  }
  const next = window.location.pathname + window.location.search;
  // 컴포넌트 밖의 공통 함수라 useRouter를 쓸 수 없고, 로그인이 풀렸으니 화면 상태도 비우는 게 맞아 전체 이동을 한다
  // eslint-disable-next-line @next/next/no-location-assign-relative-destination
  window.location.assign(`${LOGIN_PATH}?next=${encodeURIComponent(next)}`);
}

export async function apiFetch<T = void>(path: string, options: ApiFetchOptions = {}): Promise<T> {
  const { body, redirectOnUnauthorized = true, headers, ...init } = options;
  const isFormData = typeof FormData !== "undefined" && body instanceof FormData;

  const send = () =>
    fetch(path, {
      ...init,
      credentials: "same-origin",
      headers: {
        Accept: "application/json",
        ...(body !== undefined && !isFormData ? { "Content-Type": "application/json" } : {}),
        ...headers,
      },
      body: body === undefined ? undefined : isFormData ? (body as FormData) : JSON.stringify(body),
    });

  let response = await send();

  if (response.status === 401 && !isAuthRequest(path) && (await reissueOnce())) {
    response = await send();
  }

  if (!response.ok) {
    const error = new ApiError(response.status, await readErrorResponse(response));
    if (response.status === 401 && !isAuthRequest(path) && redirectOnUnauthorized) {
      goToLogin();
    }
    throw error;
  }

  if (response.status === 204 || response.headers.get("Content-Length") === "0") {
    return undefined as T;
  }
  const text = await response.text();
  return (text ? JSON.parse(text) : undefined) as T;
}
