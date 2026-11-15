# Contributing Guide

trendizm에 기여할 때 따르는 브랜치, 커밋, PR, 이슈, 의존성 규칙입니다.

- [1. 브랜치 전략](#1-브랜치-전략)
- [2. 브랜치 네이밍 규칙](#2-브랜치-네이밍-규칙)
- [3. 커밋 메시지 컨벤션](#3-커밋-메시지-컨벤션)
- [4. Pull Request](#4-pull-request)
- [5. 이슈](#5-이슈)
- [6. Backlog](#6-backlog)
- [7. 의존성 추가·변경](#7-의존성-추가변경)

---

## 1. 브랜치 전략

| 브랜치 | 역할 | 분기 기준 | 머지 대상 |
| --- | --- | --- | --- |
| `main` | 배포 브랜치 | - | - |
| `develop` | 다음 배포를 위한 통합 브랜치 | `main` | `main` |
| 작업 브랜치 (`feat/*`, `fix/*` 등) | 기능 개발, 버그 수정 등 | `develop` | `develop` |
| `hotfix/*` | 배포된 코드의 긴급 수정 | `main` | `main`, `develop` |

- `main`, `develop`에는 직접 push하지 않고, 반드시 PR을 통해 머지합니다.
- `hotfix/*`를 `main`에 머지한 뒤에는 `develop`에도 반영해 두 브랜치가 어긋나지 않게 합니다.

---

## 2. 브랜치 네이밍 규칙

```
<type>/<이슈번호>-<짧은-설명>
```

**예시**

```
feat/12-meme-search-api
fix/34-search-result-duplicate
refactor/56-meme-service
hotfix/78-login-500-error
hotfix/login-500-error         # hotfix는 이슈 없이도 가능
```

**type**

| type | 용도 |
| --- | --- |
| `feat/` | 새로운 기능 개발 |
| `add/` | 파일 추가 |
| `fix/` | 버그 수정 (`develop` 기준) |
| `hotfix/` | 배포된 코드(`main`)의 긴급 버그 수정 |
| `refactor/` | 동작 변경 없는 코드 구조 개선 |
| `perf/` | 성능 개선 |
| `style/` | 코드 포맷팅, 공백 등 동작에 영향 없는 변경 |
| `test/` | 테스트 코드 추가/수정 |
| `docs/` | 문서 작성/수정 |
| `rename/` | 파일 또는 폴더 명을 수정하거나 옮기기만 하는 작업 |
| `remove/` | 파일을 삭제만 수행한 작업 |
| `build/` | 빌드 설정, 의존성 변경 (Gradle, npm 등) |
| `ci/` | CI 설정 변경 (GitHub Actions 등) |
| `design/` | CSS 등 사용자 UI 디자인 변경 |
| `chore/` | 위에 해당하지 않는 기타 작업 |
| `revert/` | 이전 변경 되돌리기 |
| `hotfix/` | 급하게 치명적인 버그를 고쳐야 하는 작업 |

**규칙**

- 영어 소문자를 사용하고, 단어는 하이픈(`-`)으로 구분합니다. (kebab-case)
- 작업 전에 GitHub 이슈를 만들고 브랜치 이름에 이슈 번호를 붙입니다. 브랜치 이름만으로 관련 이슈를 바로 찾을 수 있습니다.
- `hotfix/`는 급한 수정이므로 이슈 없이 브랜치를 만들고 PR을 올릴 수 있습니다. 하지만 되도록 이슈 생성을 권장합니다.  (예: `hotfix/login-500-error`)
- 설명은 작업 내용을 알 수 있는 선에서 짧게(2~5단어) 작성합니다.
- 머지가 끝난 브랜치는 삭제합니다.

---

## 3. 커밋 메시지 컨벤션

커밋 메시지의 제목(첫 줄)은 아래 형식을 따릅니다. [Conventional Commits](https://www.conventionalcommits.org/ko/v1.0.0/)를 기반으로 합니다.

```
<type>: <subject>
```

### type

| type | 설명 |
| --- | --- |
| `feat:` | 새로운 기능 추가 |
| `add:` | 파일 추가 |
| `fix:` | 버그 수정 |
| `refactor:` | 동작 변경 없는 코드 구조 개선 |
| `perf:` | 성능 개선 |
| `style:` | 코드 포맷팅, 공백 등 동작에 영향 없는 변경 |
| `test:` | 테스트 코드 추가/수정 |
| `docs:` | 문서 수정 |
| `rename:` | 파일 또는 폴더 명을 수정하거나 옮기기만 하는 작업 |
| `remove:` | 파일을 삭제만 수행한 작업 |
| `build:` | 빌드 설정, 의존성 변경 (Gradle, npm 등) |
| `ci:` | CI 설정 변경 (GitHub Actions 등) |
| `design:` | CSS 등 사용자 UI 디자인 변경 |
| `chore:` | 위에 해당하지 않는 기타 작업 |
| `revert:` | 이전 커밋 되돌리기 |
| `hotfix:` | 급하게 치명적인 버그를 고쳐야 하는 작업 |

### subject

- 되도록 60자 이내로 작성합니다.
- 한국어로 작성하고, 끝에 마침표를 붙이지 않습니다.
- 무엇을 했는지 개조식으로 작성합니다. (예: `~ 추가`, `~ 수정`, `~ 구현`)

### 예시

```
feat: 밈 검색 API 구현
add: 서비스 로고 이미지 추가
fix: 검색 결과 중복 노출 문제 수정
chore: 백엔드 아키텍처 세팅, 프론트엔드 프로젝트 생성
```

---

## 4. Pull Request

### PR 제목

커밋 메시지 제목과 같은 형식과 type을 사용합니다. ([3. 커밋 메시지 컨벤션](#3-커밋-메시지-컨벤션) 참고)

```
<type>: <subject>
```

**예시**

```
add: 서비스 로고 이미지 추가
feat: 밈 검색 기능 추가
fix: 검색 결과 중복 노출 문제 수정
```

- PR 목록만 보고도 변경의 성격과 영역을 파악할 수 있게 하기 위함입니다.
- Squash merge를 사용하면 PR 제목이 그대로 커밋 메시지가 되므로, 커밋 히스토리도 일관되게 유지됩니다.
- `develop` → `main` 배포 PR은 `release: YYYY-MM-DD HH:mm` 형식으로 작성합니다. (예: `release: 2026-11-16 14:29`)

### PR 본문

PR 본문은 `.github/PULL_REQUEST_TEMPLATE.md` 양식에 맞춰 작성합니다.

### Assignee, Label

- **Assignee**: PR 작성자(작업 담당자)를 지정합니다. 본인 PR이라면 **Assign yourself**를 눌러 본인을 등록합니다.
- **Label**: 작업의 성격에 맞는 Label을 지정합니다.

### 머지 규칙

- `main`, `develop`으로의 머지는 **2명 이상의 Approve**가 있어야 가능합니다.
- PR 작성자는 자신의 PR을 approve할 수 없으므로, 작성자를 제외한 팀원 2명의 승인이 필요합니다.

### Approve 없이 머지하기 (Bypass)

> [!WARNING]
> 모든 팀원이 Admin 권한을 가지고 있어 branch protection을 우회(bypass)하고 **승인 없이 머지할 수 있습니다.**
> 하지만 이는 **정말 급한 경우에만** 사용하는 예외 수단이며, **머지 전에 반드시 신중하게 판단해야 합니다.**
> 리뷰 없이 들어간 코드는 `main`/`develop` 전체에 그대로 영향을 줍니다.

**Bypass를 고려할 수 있는 경우**

- 배포된 서비스에 장애가 발생해 리뷰를 기다릴 수 없는 핫픽스
- 일정이 촉박해서

**Bypass를 하면 안 되는 경우**

- 리뷰어가 바빠서 / 리뷰가 늦어져서
- 변경이 사소해 보여서

**Bypass로 머지했다면**

1. 머지 직후 팀 채널에 bypass 사유와 PR 링크를 공유합니다.
2. 팀원에게 사후 리뷰를 요청합니다.
3. 사후 리뷰에서 문제가 발견되면 즉시 revert하거나 후속 PR로 수정합니다.

---

## 5. 이슈

이슈는 종류에 맞는 템플릿 양식에 따라 작성합니다.

| 종류 | 템플릿 |
| --- | --- |
| 버그 제보 | `.github/ISSUE_TEMPLATE/bug.md` |
| 기능 요청 | `.github/ISSUE_TEMPLATE/feature.md` |

### Assignee, Label

- **Assignee**: 해당 작업의 담당자를 지정합니다. 본인이 담당하는 작업이라면 **Assign yourself**를 눌러 본인을 등록합니다.
- **Label**: 작업의 성격에 맞는 Label을 지정합니다.

---

## 6. Backlog

- 이슈를 등록하면 프로젝트의 **Backlog**에 추가합니다.
- 작업의 현재 상태에 맞게 Backlog에서 이슈의 상태를 지정하고, 작업이 진행되면 상태를 갱신합니다.

---

## 7. 의존성 추가·변경

npm 패키지나 Gradle 의존성은 정상 패키지라도 새 버전이 변조되어 배포될 수 있습니다. 의존성을 추가하거나 버전을 바꿀 때는 **아래 검사를 돌린 다음에** PR을 올립니다.

### 추가하기 전

- **공개된 지 7일이 지난 버전만 사용합니다.**
  - npm은 `frontend/.npmrc`의 `min-release-age=7` 설정이 자동으로 막습니다. 급한 보안 패치라 바로 받아야 하면 그 명령에만 `--min-release-age=0`을 붙이고, PR에 이유를 적습니다.
  - Gradle은 자동으로 막는 설정이 없으니 Maven Central에서 공개일을 직접 확인합니다.
- **정확한 버전으로 고정합니다.**
  - npm: `npm install --save-exact <패키지>@<버전>`
  - Gradle: `+`, `latest.release`, `-SNAPSHOT` 같은 동적 버전을 쓰지 않습니다. Spring Boot가 관리하는 의존성은 버전을 적지 않습니다.
- **npm 패키지의 설치 스크립트를 확인합니다.** `npm view <패키지>@<버전> scripts.preinstall scripts.install scripts.postinstall`로 확인하고, 설치 스크립트가 있으면 내용을 읽어 본 뒤 추가합니다.

### 추가한 다음

1. lockfile을 다시 만들고 함께 커밋합니다.
   - npm: `frontend/package-lock.json`
   - Gradle: `backend/`에서 `./gradlew dependencies --write-locks`로 `backend/gradle.lockfile`을 다시 만듭니다. 다시 만들지 않으면 빌드가 실패합니다.
2. 검사를 돌립니다.

   ```bash
   # 알려진 취약점과 악성 패키지 (OSV 데이터베이스)
   osv-scanner scan --lockfile frontend/package-lock.json
   osv-scanner scan --lockfile backend/gradle.lockfile

   # npm 레지스트리 서명 확인 (frontend/에서)
   npm audit signatures
   ```

   OSV-Scanner는 [공식 릴리스](https://github.com/google/osv-scanner/releases)에서 공개된 지 7일이 지난 버전을 받고, 같은 릴리스의 `osv-scanner_SHA256SUMS`로 체크섬을 확인한 뒤 사용합니다.
3. 추가하거나 바꾼 의존성 때문에 새로 생긴 문제가 있으면 그 의존성을 사용하지 않습니다.
4. PR 본문의 작업 내용에 추가·변경한 의존성(이름, 버전, 공개일)과 검사 결과를 적습니다.

### 외부 CDN

프론트엔드에서 외부 CDN의 스크립트나 스타일시트를 불러오지 않습니다. 실행 중에 받아오는 파일은 검사할 수 없고, CDN 쪽에서 변조되면 그대로 실행되기 때문입니다. 꼭 필요하면 팀과 상의하고, `integrity`(SRI 해시)와 `crossorigin` 속성을 붙입니다.
