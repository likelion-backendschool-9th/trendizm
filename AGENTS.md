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

- 백엔드 테스트는 H2로 실행되어 로컬 PostgreSQL 없이 돌아간다. 설정과 규칙은 [테스트](#테스트) 섹션을 따른다.
- `application.yml`은 gitignore 대상이다. 커밋하지 않고, 비밀번호 같은 값을 출력하거나 다른 파일로 옮기지 않는다.
- 프론트엔드에는 테스트 스크립트가 없다.

## 백엔드 구조 (헥사고날)

도메인 패키지(`auth`, `feed`, `item`, `lookup`, `notification`, `ranking`, `rating`)마다 같은 구조를 따른다. `feed`를 예로 들면:

```
com.trendizm.backend.feed
├── domain
│   ├── entity/Feed.java                        엔티티
│   └── enums/FeedStatus.java                   enum (상태·종류 등 정해진 값)
├── application
│   ├── port/FeedService.java                   유스케이스 인터페이스 (in 포트)
│   ├── port/in/FeedServiceImpl.java            유스케이스 구현
│   ├── port/out/FeedRepository.java            저장소 인터페이스 (out 포트)
│   ├── exception/FeedErrorCode.java            도메인 에러 코드
│   └── dto/FeedResponse.java                   응답 DTO
└── adapter
    ├── in/web/FeedController.java              REST 컨트롤러
    ├── in/web/dto/FeedRequest.java             요청 DTO
    └── out/persistence/JpaFeedRepository.java  out 포트 구현 (Spring Data)
```

- 이 구조는 "최소 변경" 같은 다른 규칙보다 우선한다. 비어 있거나 구현체가 하나뿐인 포트 인터페이스도 불필요한 추상화로 보고 생략하지 않는다.
- 컨트롤러는 `XxxService` 인터페이스에만, 서비스는 `port/out/XxxRepository`에만 의존한다. `XxxServiceImpl`과 `JpaXxxRepository`를 직접 참조하지 않는다.
- `JpaXxxRepository`는 `JpaRepository`와 out 포트를 함께 상속하는 인터페이스다. 새 조회는 out 포트에 Spring Data 메서드 이름(`findByXxx`)으로 추가한다.
- 요청 DTO는 `adapter/in/web/dto`, 응답 DTO는 `application/dto`에 record로 둔다. 응답은 `XxxResponse.from(entity)`로 만든다. 같은 도메인 `domain`(하위 `domain/entity` 포함)에 `@Entity`가 없으면(예: `auth`) 이 규칙은 적용하지 않는다.
- 엔티티는 `protected` 기본 생성자와 정적 팩토리 `create(...)`로 만들고, 수정은 엔티티 메서드를 호출해 변경 감지로 저장한다.
- 엔티티는 `domain/entity`, enum은 `domain/enums`에 둔다.
- 값이 정해진 칼럼(상태·종류 등)은 DB enum 타입을 쓰지 않는다. ERD와 DB는 `varchar(30)`, 엔티티는 `@Enumerated(EnumType.STRING)`과 `@Column(length = 30)`으로 둔다. 허용 값은 Java enum이 기준이고, ERD 칼럼 note에는 enum 위치만 적는다.
  - 이유: PostgreSQL enum 타입 칼럼에는 `@Enumerated(STRING)`이 보내는 문자열이 들어가지 않아 저장할 때 오류가 난다(42804). 로컬(`ddl-auto: update`)과 테스트(H2)에서는 드러나지 않는다.
  - 화면에 한글 이름이 필요하면 enum에 `label` 필드로 둔다. 예: `CHARGE("충전")`
- 서비스 클래스에는 `@Transactional(readOnly = true)`를 붙이고, 쓰기 메서드에만 `@Transactional`을 붙인다.
- 다른 도메인은 in 포트(`application/port/XxxService` 인터페이스)로만 호출한다.
- 다른 도메인의 엔티티(`domain`), 리포지토리(`port/out`, `JpaXxxRepository`), `XxxServiceImpl`은 직접 참조하지 않는다.
- in 포트 메서드는 엔티티 대신 DTO(`application/dto`)를 주고받는다. 다른 도메인의 DTO는 import해도 된다.
- 다른 도메인 호출은 `XxxServiceImpl`에서만 한다. 컨트롤러·어댑터에서는 호출하지 않는다.
- 도메인끼리 서로 호출하는 순환 구조(A → B, B → A)는 만들지 않는다.
- 로그인 없이 허용할 경로는 `auth/adapter/out/security/SecurityConfig`의 `permitAll` 목록에 추가한다. 새 컨트롤러를 만들면 공개 여부를 정해 `SecurityConfigTest`의 `ACCESS`에도 추가한다.
- 컨트롤러에서 로그인 사용자가 필요하면 `@AuthenticationPrincipal(expression = "id") Long userId`로 id만 받는다. `CustomUserDetails`는 auth의 Spring Security 어댑터 구현이므로 다른 도메인에서 참조하지 않는다.
- 인증은 BFF 방식이다. 브라우저는 Next 서버(`frontend/app/api`)만 부르고, Next 서버가 쿠키의 토큰을 `Authorization: Bearer` 헤더로 바꿔 백엔드에 보낸다. 백엔드는 쿠키를 읽거나 내려 주지 않고, 로그인·가입·재발급 응답 본문으로 토큰을 준다.
- `TODO(<도메인>)` 주석에 구현 순서가 적혀 있으면 그 순서를 따른다.
- 위 규칙 중 변경 감지로 저장, `findByXxx` 메서드 이름, `TODO` 순서를 뺀 나머지는 테스트로 검사한다. 규칙을 어기면 `./gradlew test`가 실패한다.
  - 구조·의존 규칙: `src/test/.../ArchitectureTest` (ArchUnit)
  - 공개 경로 규칙: `src/test/.../auth/adapter/out/security/SecurityConfigTest`

## 공통 패키지 (global)

`com.trendizm.backend.global`은 도메인이 아닌 공통 패키지다(`error`: 예외·에러 응답, `response`: 공통 응답).

- `global`의 하위 패키지(`global.error`, `global.response` 등)는 모든 도메인에서 import할 수 있다. `global` 패키지 바로 아래 클래스는 import하지 않는다.
- 반대로 `global`은 도메인 패키지를 참조하지 않는다.
- 서비스에서 예상한 실패(대상 없음, 중복, 권한 없음 등)는 `throw new BusinessException(XxxErrorCode.상수)`로 알린다. `GlobalExceptionHandler`가 `{ code, message }` 응답으로 바꾼다.
- 에러 코드는 도메인마다 `application/exception/XxxErrorCode` enum으로 만들고 `ErrorCode`를 구현한다. 특정 도메인에 속하지 않는 실패는 `CommonErrorCode`를 쓴다.
  - `code()`는 상수 이름을 그대로 쓰므로, `CommonErrorCode`나 다른 도메인과 겹치지 않게 짓는다. 예: `MEMBER_NOT_FOUND`
  - 다른 도메인의 `XxxErrorCode`는 참조하지 않는다. 그 도메인의 실패는 그 도메인 서비스가 던진다.
- `global.error.ErrorResponse`는 스프링의 `org.springframework.web.ErrorResponse`와 이름이 같다. import할 때 패키지를 확인한다.
- 목록 응답은 Spring `Page`를 그대로 내보내지 않고 `PageResponse`로 감싼다. 엔티티를 DTO로 바꾸는 일은 `XxxServiceImpl`에서 한다. 예: `PageResponse.from(feedPage, FeedResponse::from)`
- `global` 의존 방향과 다른 도메인 `XxxErrorCode` 참조 금지는 `ArchitectureTest`로 검사한다. 나머지 규칙은 테스트로 검사하지 않는다.

## 테스트

- 테스트는 H2(PostgreSQL 호환 모드)로 실행해서 Docker Compose 환경이 아닌 로컬 환경에서도 `./gradlew test`를 통해 테스트를 실행할 수 있다.
- 설정 파일: `backend/src/test/resources/application.properties` (테스트 전용이고 비밀번호 같은 민감정보 없음)
- 테스트 이름 규칙: 메서드명은 한글 허용, `@DisplayName` 사용
- 저장소 테스트는 `@SpringBootTest` + `@Transactional`로 테스트. 테스트가 끝나면 롤백.
- 테스트 데이터는 각 테스트 클래스의 `@BeforeEach`에서 엔티티를 직접 저장해 만든다. 공용 SQL 파일(`test.sql`)은 쓰지 않는다.

## 초기 데이터

| 파일 | 용도 |
| --- | --- |
| `backend/src/main/resources/data.example.sql` | 로컬에서 화면을 확인할 때 쓰는 초기 데이터 예시. 커밋한다 |
| `backend/src/main/resources/data.sql` | 위 파일을 복사해 각자 쓰는 파일. 커밋하지 않는다(`.gitignore`) |

- 테스트는 `data.sql`의 데이터에 기대지 않는다. 테스트에 필요한 데이터는 그 테스트 클래스 안에서 만든다.
- 테스트에서는 `data.sql`이 실행되지 않게 한다. H2는 내장 DB라서 기본 설정이면 `src/main/resources/data.sql`도 실행되므로, 테스트 설정에 `spring.sql.init.mode=never`를 둔다.
- 공용 `test.sql`을 쓰지 않는 이유:
  - 여러 사람이 한 파일을 같이 고치면 develop에 합칠 때 충돌이 잦고, 쓰지 않는 더미 데이터가 계속 쌓인다.
  - 테스트에 필요한 데이터를 직접 만들면, 테스트 코드만 봐도 어떤 데이터로 무엇을 검사하는지 바로 보인다.
  - 테스트가 서로 영향을 주지 않아서, 순서나 다른 사람의 작업과 상관없이 항상 같은 결과가 나온다.

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

## 운영 배포

- 운영 서버는 OpenJDK `21.0.13.1+4` 이상으로 실행한다. 그보다 낮은 버전은 [Java4Shell(CVE-2026-114938)](https://securitylabs.datadoghq.com/articles/java4shell-remote-code-execution-in-openjdk-cve-2026-114938/)에 해당한다. AVX-512 VBMI를 지원하는 x86-64 CPU에서 JDK의 Base64 디코더가 잘못된 입력을 받으면 메모리를 덮어쓴다.
  - 컨테이너 이미지에 들어 있는 JDK도 포함한다. Dockerfile 같은 배포 설정을 추가하거나 바꿀 때 베이스 이미지의 JDK 버전을 확인한다.
  - 버전은 셸의 `java -version`이 아니라 실행 중인 프로세스에서 `jcmd <PID> VM.version`으로 확인한다.
- 바로 올릴 수 없으면 JVM 옵션 `-XX:-UseBASE64Intrinsics`를 붙인다. `jcmd <PID> VM.flags -all`에 `UseBASE64Intrinsics = false`가 나오면 적용된 것이다.
- 코드 규칙([협업 규칙](#java4shellcve-2026-114938-대응))만으로는 막을 수 없다. Spring Boot 기본 설정으로 켜져 있는 multipart 요청 파싱이 로그인 없이 열린 경로에서도 각 파트의 필드 이름과 파일명을 JDK 디코더로 디코딩하기 때문이다.

## 협업 규칙

브랜치, 커밋, PR, 이슈 규칙은 [CONTRIBUTING.md](CONTRIBUTING.md)를 따른다. 자주 틀리는 것만 요약하면 다음과 같다.

- 작업 브랜치는 `develop`에서 `<type>/<이슈번호>-<짧은-설명>` 형식으로 만든다. `main`과 `develop`에는 직접 push하지 않는다.
- `hotfix/`를 제외한 모든 작업은 이슈가 있어야 시작한다. 관련 이슈가 없으면 작업 전에 사용자에게 이슈를 만들어 달라고 요청한다.
- `hotfix/`(`main`에서 분기)는 이슈 생성을 권장만 한다. 이슈 없이 브랜치를 만들고 PR을 올릴 수 있다.
- 커밋과 PR 제목은 `<type>: <subject>` 형식이다. subject는 한국어로, 60자 이내, 마침표 없이 쓴다.
- PR 본문은 `.github/PULL_REQUEST_TEMPLATE.md` 양식을 따른다.

### Java4Shell(CVE-2026-114938) 대응

[운영 배포](#운영-배포)의 JDK 기준과 함께 지킨다.

- 로컬 JDK도 `21.0.13.1+4` 이상을 쓴다. 백엔드 작업 전에 `java -version`이 이보다 낮으면 사용자에게 알린다.
- 요청 값(헤더, 쿠키, 쿼리, 본문)을 JDK Base64 디코더(`Base64.getDecoder()`, `Base64.getUrlDecoder()`)로 디코딩하는 코드를 만들지 않는다.
  - 직접 호출하지 않는다. JWT는 jjwt API로 다룬다. jjwt는 JDK 디코더 대신 자체 디코더를 쓴다.
  - Spring Security의 `httpBasic`, `rememberMe`, CSRF(기본 `XorCsrfTokenRequestAttributeHandler`)는 요청 값을 JDK 디코더로 디코딩한다. 이 기능을 켜야 하면 먼저 사용자에게 알린다.
- 로컬과 운영의 JDK가 모두 기준 버전 이상이 되면 이 항목을 지운다.

## 스킬

- 레포에 커밋하는 스킬은 `.agents/skills/skillspector` 하나뿐이다. 다른 스킬은 각자 로컬에만 두고 커밋하지 않는다(`.gitignore`로 제외되어 있다).
- 외부 스킬과 플러그인은 설치 전에 반드시 [`.agents/skills/skillspector`](.agents/skills/skillspector/SKILL.md) 스킬로 검사한다. 검사 규칙, 제공자 설정, 외부 전송 조건은 그 파일을 따른다.
