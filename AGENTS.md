# AGENTS.md

trendizm: 검색해도 안 나오는 밈, 그 출처와 의미를 함께 찾는 서비스.

## 문서 우선순위

- 이 파일(루트 `AGENTS.md`)이 기준이다.
- `frontend/AGENTS.md`, `backend/AGENTS.md`는 그 디렉터리에만 필요한 추가 내용이 있을 때만 둔다. 해당 디렉터리에서 작업할 때 참고용으로 함께 읽고, 이 파일과 충돌하면 이 파일을 따른다.
- `frontend/AGENTS.md`의 `nextjs-agent-rules` 블록은 `next dev`가 자동으로 다시 쓰므로 수정하지 않는다. 프론트엔드 전용 내용은 블록 바깥에 적는다.

## 구성

| 디렉터리 | 스택 |
| --- | --- |
| `backend/` | Spring Boot 4.1.2, Java 21, Gradle, JPA(PostgreSQL), Redis, Spring Security |
| `frontend/` | Next.js 16.4.6, React 19.3, TypeScript |

Spring Boot 4와 Next.js 16은 이전 버전과 API가 다르다. 이전 버전 사용법을 가정하지 말고 공식 문서나 의존성 소스로 확인한다.

## 명령어

| 위치 | 명령 | 용도 |
| --- | --- | --- |
| `backend/` | `./gradlew build` | 빌드(테스트 포함) |
| `backend/` | `./gradlew test` | 테스트 |
| `frontend/` | `npm run dev` | 개발 서버 |
| `frontend/` | `npm run build` | 빌드 |
| `frontend/` | `npm run lint` | 린트 |

- 백엔드 테스트용 설정이 따로 없어서 테스트도 `backend/src/main/resources/application.yml`의 PostgreSQL에 접속한다. 로컬 PostgreSQL이나 `DB_*` 환경변수가 맞지 않으면 `contextLoads`가 실패한다. 코드가 아니라 환경 문제이니 코드를 고치지 말고 사용자에게 알린다.
- `application.yml`은 gitignore 대상이다. 커밋하지 않고, 비밀번호 같은 값을 출력하거나 다른 파일로 옮기지 않는다.
- 프론트엔드에는 테스트 스크립트가 없다.

## 백엔드 구조 (헥사고날)

도메인 패키지(`auth`, `feed`, `item`, `lookup`, `notification`, `ranking`, `rating`)마다 같은 구조를 따른다. `feed`를 예로 들면:

```
com.trendizm.backend.feed
├── domain/Feed.java                            엔티티
├── application
│   ├── port/FeedService.java                   유스케이스 인터페이스 (in 포트)
│   ├── port/in/FeedServiceImpl.java            유스케이스 구현
│   ├── port/out/FeedRepository.java            저장소 인터페이스 (out 포트)
│   └── dto/FeedResponse.java                   응답 DTO
└── adapter
    ├── in/web/FeedController.java              REST 컨트롤러
    ├── in/web/dto/FeedRequest.java             요청 DTO
    └── out/persistence/JpaFeedRepository.java  out 포트 구현 (Spring Data)
```

- 이 구조는 "최소 변경" 같은 다른 규칙보다 우선한다. 비어 있거나 구현체가 하나뿐인 포트 인터페이스도 불필요한 추상화로 보고 생략하지 않는다.
- 컨트롤러는 `XxxService` 인터페이스에만, 서비스는 `port/out/XxxRepository`에만 의존한다. `XxxServiceImpl`과 `JpaXxxRepository`를 직접 참조하지 않는다.
- `JpaXxxRepository`는 `JpaRepository`와 out 포트를 함께 상속하는 인터페이스다. 새 조회는 out 포트에 Spring Data 메서드 이름(`findByXxx`)으로 추가한다.
- 요청 DTO는 `adapter/in/web/dto`, 응답 DTO는 `application/dto`에 record로 둔다. 응답은 `XxxResponse.from(entity)`로 만든다.
- 엔티티는 `protected` 기본 생성자와 정적 팩토리 `create(...)`로 만들고, 수정은 엔티티 메서드를 호출해 변경 감지로 저장한다.
- 서비스 클래스에는 `@Transactional(readOnly = true)`를 붙이고, 쓰기 메서드에만 `@Transactional`을 붙인다.
- 지금은 도메인끼리 서로 import하지 않는다. 다른 도메인을 참조해야 하면 구현 전에 사용자와 방식을 정한다.
- 로그인 없이 허용할 경로는 `auth/adapter/out/security/SecurityConfig`의 `permitAll` 목록에 추가한다. 새 API를 만들면 공개 여부를 확인한다.
- `TODO(<도메인>)` 주석에 구현 순서가 적혀 있으면 그 순서를 따른다.

## 의존성

npm·pnpm 패키지와 Gradle 의존성도 공급망 공격 대상이다. 정상 패키지의 새 버전이나, 그 패키지가 실행 중에 받아오는 파일이 변조될 수 있다.

### 추가·업그레이드 전

- 의존성을 추가하거나 버전을 바꾸기 전에 사용자에게 묻는다. 패키지 이름, 정확한 버전, 공개일, 필요한 이유를 함께 알린다.
- 공개된 지 7일이 지난 버전만 쓴다.
  - npm: `frontend/.npmrc`의 `min-release-age=7`이 강제한다. 급한 보안 패치라 예외가 필요하면 사용자 승인을 받고 그 명령에만 `--min-release-age=0`을 붙인다.
  - Gradle: 강제하는 설정이 없다. Maven Central에서 공개일을 직접 확인한다.
  - pnpm으로 바꾸면 `pnpm-workspace.yaml`에 `minimumReleaseAge: 10080`(분 단위, 7일)을 넣는다.
- 정확한 버전으로 고정한다.
  - npm: `npm install --save-exact <패키지>@<버전>`
  - Gradle: `+`, `latest.release`, `-SNAPSHOT` 같은 동적 버전을 쓰지 않는다. Spring Boot가 관리하는 의존성은 버전을 적지 않고 Spring Boot 버전을 따른다.
- npm 패키지는 설치 스크립트를 확인한다: `npm view <패키지>@<버전> scripts.preinstall scripts.install scripts.postinstall`. 있으면 내용을 보여 주고 설치할지 묻는다.

### 추가·업그레이드 후

- lockfile을 함께 커밋한다: `frontend/package-lock.json`, `backend/gradle.lockfile`.
  - Gradle 의존성을 바꾸면 `backend/`에서 `./gradlew dependencies --write-locks`로 lockfile을 다시 만든다. 다시 만들지 않으면 빌드가 실패한다.
- `package-lock.json`에 새로 들어온 항목(하위 의존성 포함) 중 `"hasInstallScript": true`가 있으면 사용자에게 알린다.
- 아래 검사를 돌린다. 추가하거나 바꾼 의존성 때문에 새로 생긴 문제가 있으면 그 의존성을 쓰지 말고 결과를 보고한다. 원래 있던 문제는 따로 알린다.
  - `osv-scanner scan --lockfile frontend/package-lock.json`, `osv-scanner scan --lockfile backend/gradle.lockfile`: OSV 데이터베이스의 알려진 취약점과 악성 패키지
  - `frontend/`에서 `npm audit signatures`: npm 레지스트리 서명 확인
- OSV-Scanner가 없으면 사용자에게 설치할지 묻는다. [공식 릴리스](https://github.com/google/osv-scanner/releases)에서 공개된 지 7일이 지난 버전을 받고, 같은 릴리스의 `osv-scanner_SHA256SUMS`로 체크섬을 확인한다.

### 외부 CDN

- 프론트엔드에서 외부 CDN의 스크립트나 스타일시트를 불러오지 않는다. 패키지로 설치하거나 직접 호스팅한다.
- 꼭 필요하면 사용자 승인을 받고, `integrity`(SRI 해시)와 `crossorigin` 속성을 붙여 받은 파일이 바뀌면 브라우저가 실행하지 않게 한다.

## 협업 규칙

브랜치, 커밋, PR, 이슈 규칙은 [CONTRIBUTING.md](CONTRIBUTING.md)를 따른다. 자주 틀리는 것만 요약하면 다음과 같다.

- 작업 브랜치는 `develop`에서 `<type>/<이슈번호>-<짧은-설명>` 형식으로 만든다. `main`과 `develop`에는 직접 push하지 않는다.
- `hotfix/`를 제외한 모든 작업은 이슈가 있어야 시작한다. 관련 이슈가 없으면 작업 전에 사용자에게 이슈를 만들어 달라고 요청한다.
- `hotfix/`(`main`에서 분기)는 이슈 생성을 권장만 한다. 이슈 없이 브랜치를 만들고 PR을 올릴 수 있다.
- 커밋과 PR 제목은 `<type>: <subject>` 형식이다. subject는 한국어로, 60자 이내, 마침표 없이 쓴다.
- PR 본문은 `.github/PULL_REQUEST_TEMPLATE.md` 양식을 따른다.

## 스킬

- 레포에 커밋하는 스킬은 `.agents/skills/skillspector` 하나뿐이다. 다른 스킬은 각자 로컬에만 두고 커밋하지 않는다(`.gitignore`로 제외되어 있다).
- 외부 스킬과 플러그인은 설치 전에 반드시 [`.agents/skills/skillspector`](.agents/skills/skillspector/SKILL.md) 스킬로 검사한다. 검사 규칙, 제공자 설정, 외부 전송 조건은 그 파일을 따른다.
