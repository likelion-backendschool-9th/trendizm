import type { NextConfig } from "next";

// 백엔드(Spring) 주소. 로컬은 기본값 그대로 쓰고, 배포에서는 BACKEND_URL 환경변수로 바꾼다.
// rewrites는 next build 때 정해지므로 배포 빌드 전에 BACKEND_URL을 설정한다.
const backendUrl = process.env.BACKEND_URL ?? "http://localhost:8080";

const nextConfig: NextConfig = {
  /* config options here */
  cacheComponents: true,
  partialPrefetching: true,
  experimental: {
    // 개발 서버가 서버 컴포넌트 디버그 정보를 웹소켓으로 브라우저에 보내는 기능.
    // 리다이렉트 페이지(/feed 등)를 오가다 보면 브라우저 콘솔에
    // "Cannot write/close a CLOSED writable stream" 오류가 나서 끈다.
    // 개발 모드 전용 기능이라 배포 빌드에는 영향이 없다.
    reactDebugChannel: false,
  },

  // 브라우저는 /api/** 를 Next.js 서버에 요청하고, Next.js가 그대로 백엔드로 전달한다(프록시).
  // 브라우저 입장에서는 같은 주소라서 CORS 설정이 필요 없고, 로그인 쿠키(HttpOnly)도 자동으로 붙는다.
  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: `${backendUrl}/api/:path*`,
      },
    ];
  },
};

export default nextConfig;
