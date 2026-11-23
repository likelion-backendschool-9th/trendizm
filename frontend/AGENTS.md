<!-- BEGIN:nextjs-agent-rules -->

## This is NOT the Next.js you know

This version has breaking changes — APIs, conventions, and file structure may all differ from your training data. Read the relevant guide in `node_modules/next/dist/docs/` (resolved from this file's directory; in monorepos the `next` package may not be visible from the repo root) before writing any code. Heed deprecation notices.

This block is written and re-added by `next dev` — verify at `node_modules/next/dist/server/lib/generate-agent-files.js`. Removing it from a diff only re-creates the uncommitted change; committing it with your work keeps the tree clean.

<!-- END:nextjs-agent-rules -->

## 백엔드 API 호출

- 클라이언트 컴포넌트에서는 `lib/api/client.ts`의 `apiFetch`, 서버 컴포넌트에서는 `lib/api/server.ts`의 `serverFetch`를 쓴다. `fetch`로 백엔드를 직접 부르지 않는다.
- 주소는 백엔드 경로 그대로 쓴다. 예: `apiFetch<PageResponse<Feed>>("/api/feeds?page=0")`.
- 인증은 BFF 방식이다. 브라우저의 `/api/**` 요청은 Next 서버의 `app/api/[...path]/route.ts`가 받아 백엔드(`BACKEND_URL`, 기본 `http://localhost:8080`)로 전달하고, `access_token` 쿠키를 `Authorization: Bearer` 헤더로 바꿔 붙인다. 로그인·가입·재발급은 `app/api/auth/*/route.ts`가 토큰을 쿠키로 저장하고 회원 정보(`LoginMember`)만 돌려준다. 로그아웃(`/api/auth/logout`)은 리프레시 토큰을 폐기하고 두 쿠키를 지운다. 로그인한 회원 정보는 `apiFetch("/api/auth/me", { redirectOnUnauthorized: false })`로 확인한다(로그인 안 했으면 401). 공통 코드는 `lib/api/bff.ts`(서버 전용)에 있다.
- 로그인 토큰은 HttpOnly 쿠키라서 자바스크립트로 읽거나 저장하지 않는다. `apiFetch`가 401을 받으면 토큰을 한 번 재발급하고 다시 요청하며, 그래도 실패하면 `/login?next=...`로 보낸다.
- 실패하면 `ApiError`(`status`, `code`, `message`, `errors`)를 던진다. 입력칸 오류는 `error.fieldMessage("email")`로 꺼낸다. 서버에 닿지 못한 경우도 `ApiError`(`code` `NETWORK_ERROR`)다. Next 서버에 못 닿으면 `status` 0, Next 서버가 백엔드에 못 닿으면 `status` 502다.
- `cacheComponents`가 켜져 있어서 서버 컴포넌트에서 `serverFetch`를 그냥 부르면 빌드가 실패한다. 공개 데이터든 로그인이 필요한 데이터(`withAuth: true`)든 지금은 그 컴포넌트를 `<Suspense>`로 감싼다.
- `"use cache"`는 쓰지 않는다. 붙인 데이터는 빌드할 때 미리 만들어져서 빌드 중에 백엔드를 부르는데, CI·배포 빌드에는 백엔드가 없어 빌드가 실패한다. 빌드 환경에서 백엔드에 접근할 수 있게 되면 그때 다시 정한다.
- 로그인 후 돌아갈 주소(`/login?next=...`)는 `/`로 시작하고 `//`로 시작하지 않는 값만 쓴다. 다른 사이트로 보내는 열린 리다이렉트를 막기 위해서다.
- 목록 API의 `page`는 0부터, 화면의 페이지 번호는 1부터다. `toApiPage`, `toUiPage`로 바꾼다.
- 응답·에러 형식과 에러 코드는 노션 WBS 1.7 "API 명세 작성" 페이지를 따른다.
