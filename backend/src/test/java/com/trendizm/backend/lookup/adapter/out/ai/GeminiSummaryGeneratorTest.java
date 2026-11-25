package com.trendizm.backend.lookup.adapter.out.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sun.net.httpserver.HttpServer;
import com.trendizm.backend.lookup.application.port.out.SummaryOpinion;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** 실제 Gemini 대신 로컬 가짜 서버(JDK HttpServer)로 요청 형식과 응답 처리를 확인한다 */
class GeminiSummaryGeneratorTest {

    private static final List<SummaryOpinion> OPINIONS = List.of(
            new SummaryOpinion("뜻1", "내용1", 3, 0), new SummaryOpinion("뜻2", "내용2", 1, 1));

    private HttpServer server;
    private final AtomicInteger status = new AtomicInteger(200);
    private final AtomicReference<String> responseBody = new AtomicReference<>();
    private final AtomicReference<String> requestPath = new AtomicReference<>();
    private final AtomicReference<String> requestKey = new AtomicReference<>();
    private final AtomicReference<String> requestBody = new AtomicReference<>();
    private final AtomicInteger requests = new AtomicInteger();
    /** true면 응답 헤더와 본문 일부만 보내고 멈춘다(본문이 중간에 멈춘 경우) */
    private final AtomicBoolean stallBody = new AtomicBoolean();
    private final CountDownLatch release = new CountDownLatch(1);
    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-25T00:00:00Z"));

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            requests.incrementAndGet();
            requestPath.set(exchange.getRequestURI().getPath());
            requestKey.set(exchange.getRequestHeaders().getFirst("x-goog-api-key"));
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] body = responseBody.get().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status.get(), body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                if (stallBody.get()) {
                    out.write(body, 0, body.length / 2);
                    out.flush();
                    release.await(10, TimeUnit.SECONDS);
                    return;
                }
                out.write(body);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        release.countDown();
        server.stop(0);
    }

    private GeminiSummaryGenerator generator(String key) {
        return generator(key, Duration.ofSeconds(5));
    }

    private GeminiSummaryGenerator generator(String key, Duration timeout) {
        return new GeminiSummaryGenerator(key, "gemini-test-lite",
                URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/v1beta/models/"), timeout, clock);
    }

    private static String candidate(String finishReason, String partsJson) {
        return "{\"candidates\":[{\"content\":{\"role\":\"model\",\"parts\":" + partsJson + "},\"finishReason\":\""
                + finishReason + "\"}]}";
    }

    @Test
    @DisplayName("모델 경로로 POST하고 키는 x-goog-api-key 헤더로 보낸다. 시스템 지시와 의견 메시지를 나눠 담고 응답 글을 돌려준다")
    void 요약_성공() {
        responseBody.set(candidate("STOP", "[{\"text\":\"  TMI는 필요 이상으로 많은 정보라는 뜻이다. \"}]"));

        assertThat(generator("test-key").summarize("tmi", OPINIONS)).contains("TMI는 필요 이상으로 많은 정보라는 뜻이다.");

        assertThat(requestPath.get()).isEqualTo("/v1beta/models/gemini-test-lite:generateContent");
        assertThat(requestKey.get()).isEqualTo("test-key");
        JsonNode body = JsonMapper.builder().build().readTree(requestBody.get());
        assertThat(body.path("systemInstruction").path("parts").path(0).path("text").asString())
                .isEqualTo(SummaryPrompt.INSTRUCTION);
        assertThat(body.path("contents").path(0).path("role").asString()).isEqualTo("user");
        assertThat(body.path("contents").path(0).path("parts").path(0).path("text").asString())
                .isEqualTo(SummaryPrompt.userMessage("tmi", OPINIONS));
        assertThat(body.path("generationConfig").path("maxOutputTokens").asInt())
                .isEqualTo(GeminiSummaryGenerator.MAX_OUTPUT_TOKENS);
    }

    @Test
    @DisplayName("생각(thought) 파트는 빼고 글 파트만 이어 붙인다")
    void 생각_파트_제외() {
        responseBody.set(candidate("STOP",
                "[{\"text\":\"내부 생각\",\"thought\":true},{\"text\":\"요약 앞부분\"},{\"text\":\" 뒷부분\"}]"));

        assertThat(generator("k").summarize("tmi", OPINIONS)).contains("요약 앞부분 뒷부분");
    }

    @Test
    @DisplayName("길이 제한(MAX_TOKENS)으로 끊긴 글은 마지막 완성 문장까지만 쓰고, 완성 문장이 없으면 말줄임표를 붙인다")
    void 길이_제한으로_끊긴_응답() {
        responseBody.set(candidate("MAX_TOKENS", "[{\"text\":\"첫 문장이다. 버전 1.5를 말한다! 세 번째 문장은 중간에\"}]"));
        assertThat(generator("k").summarize("tmi", OPINIONS)).contains("첫 문장이다. 버전 1.5를 말한다!");

        responseBody.set(candidate("MAX_TOKENS", "[{\"text\":\"완성된 문장 없이 중간에\"}]"));
        assertThat(generator("k").summarize("tmi", OPINIONS)).contains("완성된 문장 없이 중간에…");
    }

    @Test
    @DisplayName("본문이 중간에 멈추면 응답 헤더를 받은 뒤라도 전체 시간 제한에서 끊고 empty")
    void 본문_멈춤_시간_초과() {
        responseBody.set(candidate("STOP", "[{\"text\":\"" + "가".repeat(200) + "\"}]"));
        stallBody.set(true);

        long start = System.nanoTime();
        assertThat(generator("k", Duration.ofMillis(500)).summarize("tmi", OPINIONS)).isEmpty();
        assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(Duration.ofSeconds(5));
    }

    @Test
    @DisplayName("429를 받으면 retryDelay(최소 1분)만큼 모든 문서의 호출을 멈추고, 지나면 다시 부른다")
    void 한도_초과_전체_중단() {
        GeminiSummaryGenerator generator = generator("k");
        status.set(429);
        responseBody.set("{\"error\":{\"code\":429,\"details\":[{\"@type\":\"type.googleapis.com/google.rpc.RetryInfo\","
                + "\"retryDelay\":\"90s\"}]}}");

        assertThat(generator.summarize("tmi", OPINIONS)).isEmpty();
        assertThat(generator.isEnabled()).isFalse();
        assertThat(generator.summarize("다른 단어", OPINIONS)).isEmpty();
        assertThat(requests.get()).isEqualTo(1);

        clock.advance(Duration.ofSeconds(89));
        assertThat(generator.isEnabled()).isFalse();
        clock.advance(Duration.ofSeconds(1));
        status.set(200);
        responseBody.set(candidate("STOP", "[{\"text\":\"요약\"}]"));
        assertThat(generator.isEnabled()).isTrue();
        assertThat(generator.summarize("tmi", OPINIONS)).contains("요약");
        assertThat(requests.get()).isEqualTo(2);
    }

    @Test
    @DisplayName("retryDelay가 없거나 형식이 다르면 1분, 너무 길면 1시간까지만 멈춘다")
    void 한도_초과_멈추는_시간() {
        assertThat(GeminiSummaryGenerator.retryDelay("{\"error\":{\"code\":429}}")).isEmpty();
        assertThat(GeminiSummaryGenerator.retryDelay("JSON이 아님")).isEmpty();
        assertThat(GeminiSummaryGenerator.retryDelay("{\"error\":{\"details\":[{\"@type\":\"type.googleapis.com/google.rpc.RetryInfo\","
                + "\"retryDelay\":\"12.5s\"}]}}")).contains(Duration.ofSeconds(12));

        GeminiSummaryGenerator generator = generator("k");
        status.set(429);
        responseBody.set("{\"error\":{\"code\":429,\"details\":[{\"@type\":\"type.googleapis.com/google.rpc.RetryInfo\","
                + "\"retryDelay\":\"86400s\"}]}}");
        generator.summarize("tmi", OPINIONS);
        clock.advance(Duration.ofMinutes(59));
        assertThat(generator.isEnabled()).isFalse();
        clock.advance(Duration.ofMinutes(1));
        assertThat(generator.isEnabled()).isTrue();

        responseBody.set("{\"error\":{\"code\":429}}");
        generator.summarize("tmi", OPINIONS);
        clock.advance(Duration.ofSeconds(59));
        assertThat(generator.isEnabled()).isFalse();
        clock.advance(Duration.ofSeconds(1));
        assertThat(generator.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("429(무료 한도 초과)·500, 안전 필터로 막힌 응답, 빈 응답, 형식이 깨진 응답이면 empty")
    void 실패() {
        for (int code : List.of(429, 500)) {
            status.set(code);
            responseBody.set("{\"error\":{\"code\":" + code + "}}");
            assertThat(generator("k").summarize("tmi", OPINIONS)).as("status %d", code).isEmpty();
        }
        status.set(200);
        for (String body : List.of(
                candidate("SAFETY", "[{\"text\":\"막힌 글\"}]"),
                candidate("STOP", "[{\"text\":\"  \"}]"),
                "{\"promptFeedback\":{\"blockReason\":\"SAFETY\"}}",
                "{\"candidates\":[]}",
                "JSON이 아님")) {
            responseBody.set(body);
            assertThat(generator("k").summarize("tmi", OPINIONS)).as(body).isEmpty();
        }
    }

    @Test
    @DisplayName("키가 비어 있거나 헤더에 넣을 수 없는 문자(한글·공백·줄바꿈)가 있으면 꺼진 상태라 요청을 보내지 않는다. 모델 이름 형식이 이상하면 시작할 때 막는다")
    void 꺼짐과_모델_이름() {
        for (String key : List.of("  ", "AIza키 # 주석", "AIza key", "AIza\nkey")) {
            GeminiSummaryGenerator disabled = generator(key);
            assertThat(disabled.isEnabled()).as(key).isFalse();
            assertThat(disabled.summarize("tmi", OPINIONS)).as(key).isEmpty();
        }
        assertThat(requests.get()).isZero();
        assertThat(generator(" AIza-k_1 ").isEnabled()).isTrue();
        assertThatThrownBy(() -> new GeminiSummaryGenerator("k", "../other:generateContent?x=", URI.create("http://127.0.0.1/"),
                Duration.ofSeconds(1), clock))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /** 테스트에서 시간을 직접 옮기는 시계 */
    private static final class MutableClock extends Clock {

        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }
}
