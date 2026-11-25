"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";
import { apiFetch } from "@/lib/api/client";

// 액세스 토큰만 만료돼 서버가 화면을 로그인하지 않은 사람 기준으로 그렸을 때 페이지에 넣는다(fetchAsViewer.ts의 accessTokenExpired).
// 브라우저에서 토큰을 재발급하고, 성공하면 화면을 다시 그려서 내 후기·공감 여부가 바로 보이게 한다.
// 재발급이 실패하면(리프레시 토큰도 만료) 로그인하지 않은 화면을 그대로 둔다.
// 다시 그린 뒤에도 이 컴포넌트가 그대로 남아 있으면 effect가 다시 돌지 않아 반복되지 않는다.
// TODO(rating): app/item/_components/RefreshAfterReissue.tsx와 같다. fetchAsViewer와 함께 공통으로 옮긴다.
export default function RefreshAfterReissue() {
  const router = useRouter();

  useEffect(() => {
    apiFetch("/api/auth/reissue", { method: "POST" })
      .then(() => router.refresh())
      .catch(() => {});
  }, [router]);

  return null;
}
