package com.trendizm.backend.lookup.adapter.out.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetAddress;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 외부에 접속하지 않고 주소 검사만 확인한다. IP를 그대로 쓴 주소는 DNS 조회 없이 풀린다 */
class HttpWebImageClientTest {

    @Test
    @DisplayName("사설·루프백·링크 로컬(클라우드 메타데이터)·CGNAT·0.0.0.0/8·IPv6 ULA는 내부 주소로 막는다")
    void 내부_주소() throws Exception {
        for (String ip : List.of("127.0.0.1", "10.0.0.1", "172.16.0.1", "172.31.255.255", "192.168.0.10",
                "169.254.169.254", "100.64.0.1", "100.127.255.255", "0.0.0.0", "0.1.2.3", "224.0.0.1",
                "::1", "::", "fe80::1", "fc00::1", "fd12:3456::1", "::ffff:127.0.0.1", "::ffff:10.0.0.1")) {
            assertThat(HttpWebImageClient.isPublic(InetAddress.getByName(ip))).as(ip).isFalse();
        }
    }

    @Test
    @DisplayName("공인 주소는 허용한다")
    void 공인_주소() throws Exception {
        for (String ip : List.of("8.8.8.8", "1.1.1.1", "100.63.255.255", "100.128.0.1", "172.32.0.1", "2606:4700::1111")) {
            assertThat(HttpWebImageClient.isPublic(InetAddress.getByName(ip))).as(ip).isTrue();
        }
    }

    @Test
    @DisplayName("http·https와 기본 포트만, 내부 주소로 풀리는 호스트는 막는다")
    void 주소_검사() {
        assertThat(HttpWebImageClient.isAllowed(URI.create("https://8.8.8.8/a.jpg"))).isTrue();
        assertThat(HttpWebImageClient.isAllowed(URI.create("http://8.8.8.8:80/a.jpg"))).isTrue();
        for (String url : List.of("ftp://8.8.8.8/a.jpg", "file:///etc/passwd", "https://8.8.8.8:8080/a.jpg",
                "http://127.0.0.1/", "http://localhost/", "http://169.254.169.254/latest/meta-data/",
                "http://[::1]/", "http://10.0.0.1/", "https:///a.jpg")) {
            assertThat(HttpWebImageClient.isAllowed(URI.create(url))).as(url).isFalse();
        }
    }
}
