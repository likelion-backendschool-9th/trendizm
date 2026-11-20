# 배포

develop에 push되면 `.github/workflows/deploy.yml`이 백엔드 테스트 → 이미지 빌드 → GHCR push → 서버 배포를 한다.

```
develop push → GitHub Actions ─ 백엔드 테스트 (실패하면 여기서 멈춤)
                              ─ 이미지 빌드 → GHCR (ghcr.io/likelion-backendschool-9th/trendizm-*)
                              ─ ssh trendizm@cloud.newlecture.com → docker compose pull / up -d

브라우저 → https://trendizm.newlecture.com
        → 서버 nginx (443, certbot 인증서, /etc/nginx/sites-available/trendizm)
        → 127.0.0.1:3013 → frontend(Next, BFF) → backend → postgres, redis
```

- 접속 주소: https://trendizm.newlecture.com
- 바깥에서 들어오는 길은 서버 nginx 하나다. compose는 frontend만 `127.0.0.1:3013`에 열고, 나머지는 내부 네트워크에서만 접근한다.
- 테이블은 `ddl-auto: update`로 만든다. 칼럼 이름 변경·삭제는 반영되지 않는다 (아래 "DB 초기화" 참고).
- 배포 중 10~20초 동안 접속이 끊긴다. 시연·검사 시간에는 develop에 머지하지 않는다.

## 서버 구성 (이미 되어 있음)

| 항목 | 위치 |
|---|---|
| 배포 계정 | `trendizm` (비밀번호 로그인 잠금, SSH 키 전용, docker 그룹) |
| 배포 폴더 | `/home/trendizm/trendizm` (`compose.prod.yaml`은 배포 때마다 덮어씀, `.env`는 서버에만) |
| 배포 키 | `/home/trendizm/.ssh/authorized_keys` (`restrict` 옵션: 명령 실행·파일 전송만 허용) |
| nginx | `/etc/nginx/sites-available/trendizm` → `127.0.0.1:3013`, certbot이 https 부분을 추가함 (자동 갱신) |

서버 `.env` 항목 (값은 서버에서 `openssl rand -hex`로 만들었다. 채팅·Notion·GitHub에 올리지 않는다):

```
DB_NAME, DB_USERNAME, DB_PASSWORD, JWT_SECRET, COOKIE_SECURE=true
```

값에 `$`가 들어가면 docker compose가 변수로 읽어 값이 바뀐다. 영문·숫자만 쓴다.

## GitHub 설정 (이미 되어 있음)

Settings → Environments → `production` (Deployment branches: `develop`만)의 Environment secrets:

- `DEPLOY_SSH_KEY`: 배포 전용 개인키 전체
- `DEPLOY_KNOWN_HOSTS`: `cloud.newlecture.com ssh-ed25519 ...` (서버의 `/etc/ssh/ssh_host_ed25519_key.pub`로 만든 값. 가짜 서버 접속 방지)

저장소 Secrets가 아니라 Environment Secrets에 둔다. 저장소 Secrets는 아무 브랜치의 워크플로나 읽을 수 있다.

## 서버 접속

```bash
ssh <개인 계정>@cloud.newlecture.com          # 22번이 막힌 회선(SK 등)은 -p 23
sudo -u trendizm docker compose -f /home/trendizm/trendizm/compose.prod.yaml ps
sudo -u trendizm docker compose -f /home/trendizm/trendizm/compose.prod.yaml logs --tail 50 backend
```

이미지가 비공개라서 서버에서 손으로 `docker compose pull`을 하면 권한 오류가 난다. 다시 배포하려면 Actions에서 실행한다.

## 자주 하는 일

| 상황 | 방법 |
|---|---|
| 다시 배포 | Actions → Deploy → Run workflow |
| 이전 버전으로 되돌리기 | Actions → Deploy에서 정상이던 실행을 열고 Re-run all jobs (그 커밋으로 다시 빌드·배포) |
| 새 기능에 환경변수(API 키 등)가 필요 | `compose.prod.yaml`의 해당 서비스 `environment`에 추가하고, 서버 `.env`에 값을 넣는다. 값은 서버에서 직접 입력한다 |
| https 대신 http로 접속해야 하는 환경 | 서버 `.env`의 `COOKIE_SECURE=false` (https에서 false면 보안이 약해지므로 운영에서는 쓰지 않는다) |

### DB 초기화

`ddl-auto: update`는 칼럼 이름을 바꾸면 새 칼럼만 만들고 옛 칼럼(`NOT NULL`)을 남겨 저장이 실패할 수 있다. 운영 데이터가 없는 동안은 DB를 지우고 다시 만든다. 회원·데이터가 모두 지워진다.

```bash
cd /home/trendizm/trendizm
sudo -u trendizm docker compose -f compose.prod.yaml stop backend
sudo -u trendizm docker compose -f compose.prod.yaml rm -sf postgres
sudo -u trendizm docker volume rm trendizm_postgres-data
```

그다음 Actions에서 다시 배포한다. Redis(로그인 상태)는 그대로 두므로 기존 토큰은 재발급 때 회원이 없어 실패하고, 다시 로그인하면 된다.

## 확인

```bash
# Java4Shell 플래그 (UseBASE64Intrinsics = false 와 {environment}가 나오면 적용)
sudo -u trendizm docker compose -f /home/trendizm/trendizm/compose.prod.yaml exec backend \
  sh -c 'java -XX:+PrintFlagsFinal -version 2>/dev/null | grep UseBASE64Intrinsics'
```
