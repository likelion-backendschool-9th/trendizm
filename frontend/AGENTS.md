<!-- BEGIN:nextjs-agent-rules -->

## This is NOT the Next.js you know

This version has breaking changes — APIs, conventions, and file structure may all differ from your training data. Read the relevant guide in `node_modules/next/dist/docs/` (resolved from this file's directory; in monorepos the `next` package may not be visible from the repo root) before writing any code. Heed deprecation notices.

This block is written and re-added by `next dev` — verify at `node_modules/next/dist/server/lib/generate-agent-files.js`. Removing it from a diff only re-creates the uncommitted change; committing it with your work keeps the tree clean.

<!-- END:nextjs-agent-rules -->

## 백엔드 API 호출

- 클라이언트 컴포넌트에서는 `lib/api/client.ts`의 `apiFetch`, 서버 컴포넌트에서는 `lib/api/server.ts`의 `serverFetch`를 쓴다. `fetch`로 백엔드를 직접 부르지 않는다.
- 주소는 백엔드 경로 그대로 쓴다. 예: `apiFetch<PageResponse<Feed>>("/api/feeds?page=0")`. `next.config.ts`의 rewrites가 `/api/**`를 백엔드(`BACKEND_URL`, 기본 `http://localhost:8080`)로 전달한다.
- 로그인 토큰은 HttpOnly 쿠키라서 자바스크립트로 읽거나 저장하지 않는다. `apiFetch`가 401을 받으면 토큰을 한 번 재발급하고 다시 요청하며, 그래도 실패하면 `/login?next=...`로 보낸다.
- 실패하면 `ApiError`(`status`, `code`, `message`, `errors`)를 던진다. 입력칸 오류는 `error.fieldMessage("email")`로 꺼낸다.
- 목록 API의 `page`는 0부터, 화면의 페이지 번호는 1부터다. `toApiPage`, `toUiPage`로 바꾼다.
- 응답·에러 형식과 에러 코드는 노션 WBS 1.7 "API 명세 작성" 페이지를 따른다.
