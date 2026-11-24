package com.trendizm.backend.lookup.adapter.out.ai;

import com.trendizm.backend.lookup.application.port.out.MemeSummaryGenerator;
import com.trendizm.backend.lookup.application.port.out.SummaryOpinion;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Gemini API(generateContent)로 의견을 요약한다. SDK 없이 JDK HttpClient로 REST를 부른다(의존성 추가 없음).
 * - API 키: trendizm.gemini.api-key 또는 환경 변수 GEMINI_API_KEY. 비어 있으면 isEnabled()가 false라 요약 기능만 꺼진다.
 *   키는 URL이 아니라 x-goog-api-key 헤더로 보내고, 로그에 남기지 않는다.
 * - 모델: trendizm.gemini.model (기본 gemini-3.5-flash-lite, 무료 등급이 있는 가벼운 모델)
 * - 실패(429 한도 초과, 시간 초과, 안전 필터로 막힌 응답, 빈 응답)는 empty. 재시도는 하지 않는다(한도를 더 쓰지 않게)
 * 참고: 무료 등급은 보낸 내용이 Google 제품 개선에 쓰일 수 있다(Gemini API 가격 안내).
 */
@Slf4j
@Component
public class GeminiSummaryGenerator implements MemeSummaryGenerator {

    static final URI DEFAULT_BASE_URI = URI.create("https://generativelanguage.googleapis.com/v1beta/models/");
    static final Duration TIMEOUT = Duration.ofSeconds(20);
    /** 출력 토큰 상한. 요약은 300자 안쪽이라 넉넉하다 */
    static final int MAX_OUTPUT_TOKENS = 1024;
    /** 끝까지 정상으로 썼거나(STOP), 길이 제한에 걸린 경우(MAX_TOKENS, 저장할 때 자른다)만 쓴다. SAFETY 등 차단은 버린다 */
    private static final Set<String> ACCEPTED_FINISH_REASONS = Set.of("STOP", "MAX_TOKENS");
    /** 모델 이름은 설정값이지만 URL 경로에 들어가므로 형식을 확인한다 */
    private static final Pattern MODEL_NAME = Pattern.compile("^[a-z0-9][a-z0-9.-]*$");

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final HttpClient client = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
    private final String apiKey;
    private final String model;
    private final URI baseUri;

    @Autowired
    public GeminiSummaryGenerator(@Value("${trendizm.gemini.api-key:${GEMINI_API_KEY:}}") String apiKey,
                                  @Value("${trendizm.gemini.model:gemini-3.5-flash-lite}") String model) {
        this(apiKey, model, DEFAULT_BASE_URI);
    }

    /** 테스트에서 로컬 가짜 서버 주소를 넣을 때 쓴다 */
    GeminiSummaryGenerator(String apiKey, String model, URI baseUri) {
        if (!MODEL_NAME.matcher(model).matches()) {
            throw new IllegalArgumentException("trendizm.gemini.model 형식이 올바르지 않습니다: " + model);
        }
        this.apiKey = apiKey == null ? "" : apiKey.strip();
        this.model = model;
        this.baseUri = baseUri;
    }

    @Override
    public boolean isEnabled() {
        return !apiKey.isEmpty();
    }

    @Override
    public Optional<String> summarize(String word, List<SummaryOpinion> opinions) {
        if (!isEnabled()) {
            return Optional.empty();
        }
        try {
            // "./"를 붙인다. 없으면 "gemini-...:generateContent"의 콜론 앞이 URI 스킴(http: 같은)으로 해석된다
            HttpRequest request = HttpRequest.newBuilder(baseUri.resolve("./" + model + ":generateContent"))
                    .timeout(TIMEOUT)
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody(word, opinions), StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                // 429는 무료 한도 초과. 응답 본문에는 키가 없지만 길 수 있어서 상태 코드만 남긴다
                log.info("AI 요약 응답 실패 status={} model={}", response.statusCode(), model);
                return Optional.empty();
            }
            return parseText(response.body());
        } catch (IOException | JacksonException e) {
            log.info("AI 요약 호출 실패 model={}: {}", model, e.toString());
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    static String requestBody(String word, List<SummaryOpinion> opinions) {
        Map<String, Object> body = Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", SummaryPrompt.INSTRUCTION))),
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", SummaryPrompt.userMessage(word, opinions))))),
                "generationConfig", Map.of("maxOutputTokens", MAX_OUTPUT_TOKENS));
        return JSON.writeValueAsString(body);
    }

    /** candidates[0].content.parts[].text를 이어 붙인다. 생각(thought) 파트는 뺀다. 막혔거나 비었으면 empty */
    static Optional<String> parseText(String responseBody) {
        JsonNode root = JSON.readTree(responseBody);
        JsonNode candidate = root.path("candidates").path(0);
        String finishReason = candidate.path("finishReason").asString("");
        if (candidate.isMissingNode() || !ACCEPTED_FINISH_REASONS.contains(finishReason)) {
            log.info("AI 요약 응답 사용 안 함 finishReason={} blockReason={}",
                    finishReason, root.path("promptFeedback").path("blockReason").asString(""));
            return Optional.empty();
        }
        StringBuilder text = new StringBuilder();
        for (JsonNode part : candidate.path("content").path("parts")) {
            if (!part.path("thought").asBoolean(false)) {
                text.append(part.path("text").asString(""));
            }
        }
        String summary = text.toString().strip();
        return summary.isEmpty() ? Optional.empty() : Optional.of(summary);
    }
}
