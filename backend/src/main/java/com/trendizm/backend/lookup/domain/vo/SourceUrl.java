package com.trendizm.backend.lookup.domain.vo;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 밈 출처 주소 정규화. 같은 게시물을 가리키는 모바일·웹·공유 링크를 같은 문자열로 맞춰서,
 * (word, source_url) UNIQUE가 같은 출처를 같은 문서로 보게 한다.
 *
 * <p>플랫폼별 결과 (유튜브·X·인스타그램·스레드·틱톡):
 * <ul>
 *   <li>유튜브: youtu.be/ID, m.youtube.com/watch?v=ID&amp;t=30s, youtube.com/shorts/ID → https://youtube.com/watch?v=ID</li>
 *   <li>X: twitter.com·mobile.twitter.com·x.com/계정/status/ID → https://x.com/i/status/ID (계정 이름이 바뀌어도 같은 글)</li>
 *   <li>인스타그램: instagram.com/p/코드, /reel/코드, /reels/코드, /tv/코드 → https://instagram.com/p/코드</li>
 *   <li>스레드: threads.net·threads.com/@계정/post/코드 → https://threads.com/@계정/post/코드</li>
 *   <li>틱톡: tiktok.com/@계정/video/ID → https://tiktok.com/@계정/video/ID.
 *       vm.tiktok.com 같은 단축 링크는 접속해 봐야 원래 주소를 알 수 있어서 그대로 둔다(서버는 외부에 접속하지 않는다)</li>
 * </ul>
 * 그 밖의 주소는 https로 통일, 호스트 소문자, 앞의 www.·m.·mobile. 제거, # 뒤 제거, 추적 파라미터(utm_* 등) 제거, 끝 / 제거만 한다.
 * 주소로 해석할 수 없는 값은 앞뒤 공백만 지운다.
 *
 * <p>썸네일: 유튜브는 영상 ID로 썸네일 주소가 정해져 있어서(youtubeThumbnail) 외부에 접속하지 않고 만든다.
 * 다른 플랫폼은 규칙이 없어서 만들지 않는다(화면에서 기본 이미지를 보여 준다).
 */
public final class SourceUrl {

    /** 일반 주소에서 지우는 공유·광고 추적 파라미터 (utm_*는 따로 지운다). 페이지 내용을 바꿀 수 있는 이름은 넣지 않는다 */
    private static final Set<String> TRACKING_PARAMS = Set.of("fbclid", "gclid", "igsh", "igshid", "si", "feature");

    private static final Pattern YOUTUBE_PATH = Pattern.compile("^/(?:shorts|embed|live|v)/([\\w-]+)");
    private static final Pattern X_STATUS = Pattern.compile("^/(?:[^/]+|i/web|i)/status(?:es)?/(\\d+)");
    private static final Pattern INSTAGRAM_POST = Pattern.compile("^/(?:[^/]+/)?(?:p|reel|reels|tv)/([\\w-]+)");
    private static final Pattern THREADS_POST = Pattern.compile("^/(@[^/]+)/post/([\\w-]+)");
    private static final Pattern TIKTOK_VIDEO = Pattern.compile("^/(@[^/]+)/video/(\\d+)");
    /** normalize가 만든 유튜브 주소. 영상 ID는 영문·숫자·_·-만 받는다 */
    private static final Pattern NORMALIZED_YOUTUBE = Pattern.compile("^https://youtube\\.com/watch\\?v=([\\w-]+)$");

    private SourceUrl() {
    }

    public static String normalize(String raw) {
        String value = raw.strip();
        URI uri;
        try {
            uri = new URI(value);
        } catch (URISyntaxException e) {
            return value;
        }
        if (uri.getHost() == null) {
            return value;
        }
        String host = stripPrefix(uri.getHost().toLowerCase(Locale.ROOT));
        String path = uri.getRawPath() == null ? "" : uri.getRawPath();
        String query = uri.getRawQuery();

        String platform = platformUrl(host, path, query);
        if (platform != null) {
            return platform;
        }
        String cleanedQuery = removeTrackingParams(query);
        String cleanedPath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        return "https://" + host + portPart(uri) + cleanedPath + (cleanedQuery.isEmpty() ? "" : "?" + cleanedQuery);
    }

    /**
     * 정규화한 출처가 유튜브 영상이면 그 썸네일 주소. 예: https://youtube.com/watch?v=abc → https://i.ytimg.com/vi/abc/hqdefault.jpg
     * 유튜브 영상이 아니면 empty
     */
    public static Optional<String> youtubeThumbnail(String normalizedUrl) {
        Matcher matcher = NORMALIZED_YOUTUBE.matcher(normalizedUrl);
        return matcher.matches()
                ? Optional.of("https://i.ytimg.com/vi/" + matcher.group(1) + "/hqdefault.jpg")
                : Optional.empty();
    }

    /** 플랫폼 게시물 주소면 정해진 형태로, 아니면 null */
    private static String platformUrl(String host, String path, String query) {
        switch (host) {
            case "youtu.be" -> {
                String id = firstSegment(path);
                return id.isEmpty() ? null : "https://youtube.com/watch?v=" + id;
            }
            case "youtube.com", "music.youtube.com" -> {
                String id = path.equals("/watch") ? queryParam(query, "v") : match(YOUTUBE_PATH, path, 1);
                return id == null || id.isEmpty() ? null : "https://youtube.com/watch?v=" + id;
            }
            case "x.com", "twitter.com" -> {
                String id = match(X_STATUS, path, 1);
                return id == null ? null : "https://x.com/i/status/" + id;
            }
            case "instagram.com" -> {
                String code = match(INSTAGRAM_POST, path, 1);
                return code == null ? null : "https://instagram.com/p/" + code;
            }
            case "threads.net", "threads.com" -> {
                Matcher matcher = THREADS_POST.matcher(path);
                return matcher.find()
                        ? "https://threads.com/" + matcher.group(1).toLowerCase(Locale.ROOT) + "/post/" + matcher.group(2)
                        : null;
            }
            case "tiktok.com" -> {
                Matcher matcher = TIKTOK_VIDEO.matcher(path);
                return matcher.find()
                        ? "https://tiktok.com/" + matcher.group(1).toLowerCase(Locale.ROOT) + "/video/" + matcher.group(2)
                        : null;
            }
            default -> {
                return null;
            }
        }
    }

    private static String stripPrefix(String host) {
        for (String prefix : List.of("www.", "m.", "mobile.")) {
            if (host.startsWith(prefix)) {
                return host.substring(prefix.length());
            }
        }
        return host;
    }

    private static String portPart(URI uri) {
        int port = uri.getPort();
        return port == -1 || port == 80 || port == 443 ? "" : ":" + port;
    }

    private static String firstSegment(String path) {
        String trimmed = path.startsWith("/") ? path.substring(1) : path;
        int slash = trimmed.indexOf('/');
        return slash == -1 ? trimmed : trimmed.substring(0, slash);
    }

    private static String match(Pattern pattern, String path, int group) {
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? matcher.group(group) : null;
    }

    private static String queryParam(String query, String name) {
        if (query == null) {
            return null;
        }
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0 && pair.substring(0, eq).equals(name)) {
                return pair.substring(eq + 1);
            }
        }
        return null;
    }

    private static String removeTrackingParams(String query) {
        if (query == null || query.isEmpty()) {
            return "";
        }
        List<String> kept = new ArrayList<>();
        for (String pair : query.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            int eq = pair.indexOf('=');
            String name = (eq == -1 ? pair : pair.substring(0, eq)).toLowerCase(Locale.ROOT);
            if (!name.startsWith("utm_") && !TRACKING_PARAMS.contains(name)) {
                kept.add(pair);
            }
        }
        return String.join("&", kept);
    }
}
