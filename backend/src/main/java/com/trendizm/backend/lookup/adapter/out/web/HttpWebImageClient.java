package com.trendizm.backend.lookup.adapter.out.web;

import com.trendizm.backend.lookup.application.port.out.DownloadedImage;
import com.trendizm.backend.lookup.application.port.out.WebImageClient;
import com.trendizm.backend.lookup.domain.enums.ThumbnailImageType;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 출처 페이지의 메타데이터에서 대표 이미지를 찾고 받아 온다. 사용자가 넣은 주소에 서버가 접속하므로 다음을 지킨다.
 * - http·https, 기본 포트(80·443)만. 주소의 호스트가 내부망(사설·루프백·링크 로컬 등)으로 풀리면 접속하지 않는다.
 *   Docker 안에서는 backend·postgres·redis 같은 컨테이너 이름도 사설 IP로 풀려서 함께 막힌다.
 * - 리다이렉트는 자동으로 따라가지 않고, 최대 MAX_REDIRECTS번까지 새 주소를 같은 규칙으로 다시 검사한 뒤 따라간다.
 * - 시간(TIMEOUT)과 크기(페이지 MAX_PAGE_BYTES, 이미지 MAX_IMAGE_BYTES)를 제한한다.
 * - 이미지는 Content-Type과 파일 앞부분이 모두 허용한 형식(ThumbnailImageType)이어야 받는다.
 * 한계: 검사할 때와 실제 접속할 때 DNS를 따로 조회해서, 그 사이 응답을 바꾸는 공격(DNS 리바인딩)은 막지 못한다.
 * 시연용 범위라 받아들였다. 운영한다면 내부망으로 나가는 길이 막힌 별도 네트워크에서 실행한다.
 */
@Slf4j
@Component
public class HttpWebImageClient implements WebImageClient {

    static final Duration TIMEOUT = Duration.ofSeconds(5);
    static final int MAX_REDIRECTS = 3;
    static final int MAX_PAGE_BYTES = 1024 * 1024;
    static final int MAX_IMAGE_BYTES = 5 * 1024 * 1024;
    /** 크롤러인 척하지 않고 우리 서비스 이름을 밝힌다 */
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; trendizm-thumbnail/1.0)";

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    @Override
    public Optional<String> findImageUrl(String pageUrl) {
        return fetch(pageUrl, "text/html,application/xhtml+xml", MAX_PAGE_BYTES)
                .filter(page -> page.contentType() != null && page.contentType().toLowerCase(Locale.ROOT).contains("html"))
                .flatMap(page -> MetaImageParser.find(new String(page.body(), StandardCharsets.UTF_8), page.uri()));
    }

    @Override
    public Optional<DownloadedImage> download(String imageUrl) {
        return fetch(imageUrl, "image/jpeg,image/png,image/webp,image/gif", MAX_IMAGE_BYTES)
                .flatMap(image -> ThumbnailImageType.fromContentType(image.contentType())
                        .filter(type -> type.matches(image.body()))
                        .map(type -> new DownloadedImage(image.body(), type)));
    }

    /** 받은 응답. uri는 리다이렉트를 따라간 뒤의 최종 주소다(페이지 안의 상대 주소를 이 기준으로 푼다) */
    private record Fetched(URI uri, String contentType, byte[] body) {
    }

    private Optional<Fetched> fetch(String url, String accept, int maxBytes) {
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        try {
            for (int redirects = 0; redirects <= MAX_REDIRECTS; redirects++) {
                if (!isAllowed(uri)) {
                    log.info("썸네일 접속 거부 {}", uri);
                    return Optional.empty();
                }
                HttpRequest request = HttpRequest.newBuilder(uri)
                        .timeout(TIMEOUT)
                        .header("User-Agent", USER_AGENT)
                        .header("Accept", accept)
                        .GET()
                        .build();
                HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
                try (InputStream body = response.body()) {
                    int status = response.statusCode();
                    Optional<String> location = response.headers().firstValue("Location");
                    if (status >= 300 && status < 400 && location.isPresent()) {
                        uri = uri.resolve(location.get());
                        continue;
                    }
                    if (status != 200) {
                        return Optional.empty();
                    }
                    byte[] bytes = body.readNBytes(maxBytes + 1);
                    if (bytes.length > maxBytes) {
                        return Optional.empty();
                    }
                    return Optional.of(new Fetched(uri, response.headers().firstValue("Content-Type").orElse(null), bytes));
                }
            }
            return Optional.empty();
        } catch (IOException | IllegalArgumentException e) {
            log.info("썸네일 접속 실패 {}: {}", uri, e.toString());
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    /** http·https, 기본 포트이고, 호스트가 풀리는 주소가 모두 공인 주소여야 한다 */
    static boolean isAllowed(URI uri) {
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!scheme.equals("http") && !scheme.equals("https")) {
            return false;
        }
        int port = uri.getPort();
        if (port != -1 && port != 80 && port != 443) {
            return false;
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            return false;
        }
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress address : addresses) {
                if (!isPublic(address)) {
                    return false;
                }
            }
            return addresses.length > 0;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /** 사설(10/8, 172.16/12, 192.168/16)·루프백·링크 로컬(169.254/16, 클라우드 메타데이터 포함)·0.0.0.0/8·CGNAT(100.64/10)·IPv6 ULA(fc00::/7)가 아니면 공인 주소로 본다 */
    static boolean isPublic(InetAddress address) {
        if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                || address.isSiteLocalAddress() || address.isMulticastAddress()) {
            return false;
        }
        byte[] bytes = address.getAddress();
        if (address instanceof Inet4Address) {
            int first = bytes[0] & 0xFF;
            int second = bytes[1] & 0xFF;
            return first != 0 && !(first == 100 && second >= 64 && second <= 127);
        }
        if (address instanceof Inet6Address) {
            return (bytes[0] & 0xFE) != 0xFC;
        }
        return false;
    }
}
