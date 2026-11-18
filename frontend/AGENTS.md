<!-- BEGIN:nextjs-agent-rules -->

## This is NOT the Next.js you know

This version has breaking changes — APIs, conventions, and file structure may all differ from your training data. Read the relevant guide in `node_modules/next/dist/docs/` (resolved from this file's directory; in monorepos the `next` package may not be visible from the repo root) before writing any code. Heed deprecation notices.

This block is written and re-added by `next dev` — verify at `node_modules/next/dist/server/lib/generate-agent-files.js`. Removing it from a diff only re-creates the uncommitted change; committing it with your work keeps the tree clean.

<!-- END:nextjs-agent-rules -->

## 백엔드 API 호출

- 클라이언트 컴포넌트에서는 `lib/api/client.ts`의 `apiFetch`, 서버 컴포넌트에서는 `lib/api/server.ts`의 `serverFetch`를 쓴다. `fetch`로 백엔드를 직접 부르지 않는다.
- 주소는 백엔드 경로 그대로 쓴다. 예: `apiFetch<PageResponse<Feed>>("/api/feeds?page=0")`. `next.config.ts`의 rewrites가 `/api/**`를 백엔드(`BACKEND_URL`, 기본 `http://localhost:8080`)로 전달한다.
- 로그인 토큰은 HttpOnly 쿠키라서 자바스크립트로 읽거나 저장하지 않는다. `apiFetch`가 401을 받으면 토큰을 한 번 재발급하고 다시 요청하며, 그래도 실패하면 `/login?next=...`로 보낸다.
- 실패하면 `ApiError`(`status`, `code`, `message`, `errors`)를 던진다. 입력칸 오류는 `error.fieldMessage("email")`로 꺼낸다. 서버에 닿지 못한 경우도 `ApiError`(`status` 0, `code` `NETWORK_ERROR`)다.
- `cacheComponents`가 켜져 있어서 서버 컴포넌트에서 `serverFetch`를 그냥 부르면 빌드가 실패한다. 공개 데이터는 읽는 함수·컴포넌트에 `"use cache"`를 붙이고, 로그인이 필요한 데이터(`withAuth: true`)는 그 컴포넌트를 `<Suspense>`로 감싼다.
- 로그인 후 돌아갈 주소(`/login?next=...`)는 `/`로 시작하고 `//`로 시작하지 않는 값만 쓴다. 다른 사이트로 보내는 열린 리다이렉트를 막기 위해서다.
- 목록 API의 `page`는 0부터, 화면의 페이지 번호는 1부터다. `toApiPage`, `toUiPage`로 바꾼다.
- 응답·에러 형식과 에러 코드는 노션 WBS 1.7 "API 명세 작성" 페이지를 따른다.
