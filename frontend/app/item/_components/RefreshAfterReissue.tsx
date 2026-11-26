"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";
import { apiFetch } from "@/lib/api/client";

// 서버가 화면을 액세스 토큰 없이(로그인하지 않은 사람 기준으로) 그렸을 때 페이지에 넣는다(fetchAsViewer.ts의 renderedAsGuest).
// 브라우저에서 로그인을 확인한다. /api/auth/me는 401이면 apiFetch가 토큰을 재발급하고 다시 부른다.
// - 성공하면 액세스 토큰만 만료됐던 것이라(지금은 새 토큰이 있음) 화면을 다시 그려서 연 아이템·공감 여부 등이 바로 보이게 한다.
// - 실패하면(로그아웃 상태, 리프레시 토큰도 만료) 로그인하지 않은 화면을 그대로 둔다.
// 헤더도 같은 확인을 하지만, 같은 탭의 재발급은 apiFetch가 한 번으로 묶어서 두 번 나가지 않는다.
// 다시 그린 뒤에도 이 컴포넌트가 그대로 남아 있으면 effect가 다시 돌지 않아 반복되지 않는다.
export default function RefreshAfterReissue() {
  const router = useRouter();

  useEffect(() => {
    let active = true;
    apiFetch("/api/auth/me", { redirectOnUnauthorized: false })
      .then(() => {
        if (active) {
          router.refresh();
        }
      })
      .catch(() => {});
    return () => {
      active = false;
    };
  }, [router]);

  return null;
}
