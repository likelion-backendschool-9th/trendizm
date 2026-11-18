import type { NextConfig } from "next";

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
};

export default nextConfig;
