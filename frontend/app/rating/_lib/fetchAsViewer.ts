// 서버 컴포넌트에서, 보는 사람에 따라 응답이 달라지는 공개 API(내 후기 여부, 공감 여부)를 부를 때 쓴다.
// 로그인 쿠키가 있으면 같이 보내고, 401이면 로그인하지 않은 사람으로 다시 부른다.
// TODO(rating): app/item/_lib/fetchAsViewer.ts와 같다. lib/api로 합친다.

import { cookies } from "next/headers";
import { ACCESS_TOKEN_COOKIE, REFRESH_TOKEN_COOKIE } from "@/lib/api/bff";
import { serverFetch } from "@/lib/api/server";
import { ApiError } from "@/lib/api/types";

export async function fetchAsViewer<T>(path: string): Promise<T> {
  try {
    return await serverFetch<T>(path, { withAuth: true });
  } catch (error) {
    if (error instanceof ApiError && error.status === 401) {
      return serverFetch<T>(path);
    }
    throw error;
  }
}

/**
 * 로그인은 유지되는데 액세스 토큰만 만료된 상태인지.
 * 액세스 토큰 쿠키는 토큰 수명과 같이 사라지므로, 리프레시 토큰 쿠키만 남아 있으면 이 상태다.
 * 이때 공개 API는 로그인하지 않은 사람 기준으로 응답하므로(백엔드는 토큰이 없으면 비로그인으로 처리),
 * 페이지는 <RefreshAfterReissue />를 그려서 브라우저가 재발급한 뒤 화면을 다시 그리게 한다.
 * (서버 컴포넌트는 쿠키를 새로 쓸 수 없어서 여기서 재발급할 수 없다)
 */
export async function accessTokenExpired(): Promise<boolean> {
  const cookieStore = await cookies();
  return cookieStore.has(REFRESH_TOKEN_COOKIE) && !cookieStore.has(ACCESS_TOKEN_COOKIE);
}
