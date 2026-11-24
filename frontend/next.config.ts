import type { NextConfig } from "next";

/**
 * 모든 응답에 붙이는 보안 헤더
 * - frame-ancestors / X-Frame-Options: 다른 사이트가 우리 화면을 iframe으로 겹쳐 클릭을 유도하는 것(클릭재킹)을 막는다.
 *   X-Frame-Options는 frame-ancestors를 모르는 오래된 브라우저용이다.
 * - Strict-Transport-Security: 한 번 https로 접속한 브라우저는 1년 동안 http로 접속하지 않는다(http에서는 브라우저가 무시한다).
 *   하위 도메인 강제(includeSubDomains)와 preload는 넣지 않는다. 도메인(newlecture.com)을 다른 팀과 같이 쓰기 때문이다.
 * - X-Content-Type-Options: 브라우저가 파일 형식을 추측해 스크립트로 실행하지 못하게 한다.
 * - Referrer-Policy: 다른 사이트로 이동할 때 주소 전체(경로·쿼리) 대신 출처만 보낸다.
 * - Permissions-Policy: 쓰지 않는 카메라·마이크·위치 권한을 끈다.
 * CSP는 frame-ancestors만 둔다. 스크립트 제한(script-src)까지 넣으려면 Next의 nonce 설정이 필요해서 따로 한다.
 */
const SECURITY_HEADERS = [
  { key: "Content-Security-Policy", value: "frame-ancestors 'none'" },
  { key: "X-Frame-Options", value: "DENY" },
  { key: "Strict-Transport-Security", value: "max-age=31536000" },
  { key: "X-Content-Type-Options", value: "nosniff" },
  { key: "Referrer-Policy", value: "strict-origin-when-cross-origin" },
  { key: "Permissions-Policy", value: "camera=(), microphone=(), geolocation=()" },
];

const nextConfig: NextConfig = {
  // 배포 이미지(frontend/Dockerfile)에 실행에 필요한 파일만 담는다
  output: "standalone",
  // 응답에 X-Powered-By: Next.js를 붙이지 않는다(서버 종류를 알려 주지 않게)
  poweredByHeader: false,
  async headers() {
    return [{ source: "/:path*", headers: SECURITY_HEADERS }];
  },
  cacheComponents: true,
  partialPrefetching: true,
  experimental: {
    // 개발 서버가 서버 컴포넌트 디버그 정보를 웹소켓으로 브라우저에 보내는 기능.
    // 리다이렉트 페이지(/feed 등)를 오가다 보면 브라우저 콘솔에
    // "Cannot write/close a CLOSED writable stream" 오류가 나서 끈다.
    // 개발 모드 전용 기능이라 배포 빌드에는 영향이 없다.
    reactDebugChannel: false,
  },
};

export default nextConfig;
