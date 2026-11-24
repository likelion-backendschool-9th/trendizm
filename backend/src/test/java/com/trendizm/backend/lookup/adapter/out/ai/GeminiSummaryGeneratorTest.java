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
import java.util.List;
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
                out.write(body);
            }
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private GeminiSummaryGenerator generator(String key) {
        return new GeminiSummaryGenerator(key, "gemini-test-lite",
                URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/v1beta/models/"));
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
    @DisplayName("생각(thought) 파트는 빼고 글 파트만 이어 붙인다. 길이 제한(MAX_TOKENS)에 걸린 글도 쓴다")
    void 생각_파트_제외() {
        responseBody.set(candidate("MAX_TOKENS",
                "[{\"text\":\"내부 생각\",\"thought\":true},{\"text\":\"요약 앞부분\"},{\"text\":\" 뒷부분\"}]"));

        assertThat(generator("k").summarize("tmi", OPINIONS)).contains("요약 앞부분 뒷부분");
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
    @DisplayName("키가 비어 있으면 꺼진 상태라 요청을 보내지 않는다. 모델 이름 형식이 이상하면 시작할 때 막는다")
    void 꺼짐과_모델_이름() {
        GeminiSummaryGenerator disabled = generator("  ");

        assertThat(disabled.isEnabled()).isFalse();
        assertThat(disabled.summarize("tmi", OPINIONS)).isEmpty();
        assertThat(requests.get()).isZero();
        assertThat(generator("k").isEnabled()).isTrue();
        assertThatThrownBy(() -> new GeminiSummaryGenerator("k", "../other:generateContent?x=", URI.create("http://127.0.0.1/")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
