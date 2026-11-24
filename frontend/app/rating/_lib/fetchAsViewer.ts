// 서버 컴포넌트에서, 보는 사람에 따라 응답이 달라지는 공개 API(내 후기 여부, 공감 여부)를 부를 때 쓴다.
// 로그인 쿠키가 있으면 같이 보내고, 액세스 토큰이 만료돼 401이면 로그인하지 않은 사람으로 다시 부른다.
// (서버 컴포넌트는 쿠키를 새로 쓸 수 없어서 재발급을 못 한다. 버튼을 누르면 apiFetch가 재발급한다)
// TODO(rating): 트렌드 아이템 PR(#49)의 app/item/_lib/fetchAsViewer.ts와 같다. 둘 다 머지되면 lib/api로 합친다.

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
