/**
 * 로그인 후 돌아갈 주소(?next=)를 같은 사이트 안의 주소로만 바꾼다(열린 리다이렉트 방지).
 *
 * 문자열 규칙("//"로 시작하면 막기 등)으로 막지 않는다. 브라우저는 주소 속 탭·줄바꿈을 지우고,
 * "/./", "/../" 같은 경로를 정리하고, "\"를 "/"로 읽는 등 규칙이 많아서 하나라도 빠지면 다른 사이트로 나간다.
 * 그래서 브라우저와 같은 방식(new URL)으로 해석해 출처(origin)를 비교하고, 검사한 전체 주소(href)를 그대로 돌려준다.
 * 경로(pathname)만 꺼내 쓰면 "/.//evil.com"이 "//evil.com"으로 정리되어 다시 다른 사이트로 해석되므로 쓰지 않는다.
 *
 * @param next ?next= 값. 없으면 null
 * @param origin 우리 사이트 출처. 브라우저에서는 window.location.origin
 * @returns 같은 사이트면 그 주소 전체, 아니면 우리 사이트의 "/"
 */
export function sameOriginUrl(next: string | null, origin: string): string {
  const home = new URL("/", origin).href;
  if (!next) {
    return home;
  }
  let url: URL;
  try {
    url = new URL(next, origin);
  } catch {
    return home;
  }
  if (url.origin !== new URL(origin).origin) {
    return home;
  }
  // "https://evil.com@우리사이트/"처럼 붙은 사용자 정보는 지운다(주소창에 남아 헷갈리게 하지 않게)
  url.username = "";
  url.password = "";
  return url.href;
}
