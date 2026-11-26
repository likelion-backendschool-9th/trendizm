// 서버 컴포넌트에서, 보는 사람에 따라 응답이 달라지는 공개 API(내 글 여부, 연 아이템, 공감 여부)를 부를 때 쓴다.
// 로그인 쿠키가 있으면 같이 보내고, 401이면 로그인하지 않은 사람으로 다시 부른다.

import { cookies } from "next/headers";
import { ACCESS_TOKEN_COOKIE } from "@/lib/api/bff";
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
 * 액세스 토큰 없이(로그인하지 않은 사람 기준으로) 그리는지.
 * 이때 공개 API는 로그인하지 않은 사람 기준으로 응답한다(백엔드는 토큰이 없으면 비로그인으로 처리).
 * 액세스 토큰만 만료돼 로그인은 유지되는 경우도 여기에 들어가므로, 페이지는 <RefreshAfterReissue />를 그려서
 * 브라우저가 로그인을 확인(필요하면 재발급)한 뒤 화면을 다시 그리게 한다.
 * - 리프레시 토큰 쿠키는 /api/auth 경로에만 붙어서 페이지 요청에는 오지 않는다. 그래서 서버에서는
 *   "만료"와 "로그아웃"을 구분할 수 없고, 구분은 브라우저(RefreshAfterReissue)가 한다.
 * - 서버 컴포넌트는 쿠키를 새로 쓸 수 없어서 여기서 재발급할 수 없다.
 */
export async function renderedAsGuest(): Promise<boolean> {
  const cookieStore = await cookies();
  return !cookieStore.has(ACCESS_TOKEN_COOKIE);
}
