package com.trendizm.backend.lookup.adapter.out.ai;

import com.trendizm.backend.lookup.application.port.out.MemeSummaryGenerator;
import com.trendizm.backend.lookup.application.port.out.SummaryOpinion;
import com.trendizm.backend.lookup.domain.vo.SummaryText;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
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
 *   헤더에 넣을 수 없는 문자(한글 주석, 줄바꿈 등)가 섞여 있으면 값은 남기지 않고 경고만 남긴 뒤 요약 기능을 끈다
 *   (그대로 보내면 JDK가 키 값을 담은 예외를 던져 로그에 키가 남는다).
 * - 모델: trendizm.gemini.model (기본 gemini-3.5-flash-lite, 무료 등급이 있는 가벼운 모델)
 * - 시간 제한: 요청부터 응답 본문을 다 받을 때까지 전체에 TIMEOUT을 건다. HttpRequest.timeout은 응답 헤더를 받을 때까지만
 *   적용돼서, 본문이 중간에 멈추면 스레드가 풀리지 않는다. 그래서 sendAsync로 보내고 기다리는 시간을 따로 제한한다.
 * - 429(한도 초과): 한도는 API 키(프로젝트) 단위라 다른 문서 요청도 실패한다. 그래서 받으면 blockedUntil까지
 *   isEnabled()를 false로 해서 모든 문서의 호출을 멈춘다. 응답의 RetryInfo.retryDelay가 있으면 그만큼(RATE_LIMIT_BLOCK~MAX_RATE_LIMIT_BLOCK),
 *   없으면 RATE_LIMIT_BLOCK만큼 멈춘다. 시각은 서버 메모리에만 둔다(서버가 한 대라 충분하다).
 * - 길이 제한(MAX_TOKENS)으로 끊긴 응답은 마지막 완성 문장까지만 쓴다(SummaryText.endAtSentence).
 * - 실패(429, 시간 초과, 안전 필터로 막힌 응답, 빈 응답)는 empty. 재시도는 하지 않는다(한도를 더 쓰지 않게)
 * 참고: 무료 등급은 보낸 내용이 Google 제품 개선에 쓰일 수 있다(Gemini API 가격 안내).
 */
@Slf4j
@Component
public class GeminiSummaryGenerator implements MemeSummaryGenerator {

    static final URI DEFAULT_BASE_URI = URI.create("https://generativelanguage.googleapis.com/v1beta/models/");
    static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(20);
    /** 429를 받은 뒤 모든 호출을 멈추는 기본 시간 */
    static final Duration RATE_LIMIT_BLOCK = Duration.ofMinutes(1);
    /** retryDelay가 너무 길게 와도 이 이상은 멈추지 않는다 */
    static final Duration MAX_RATE_LIMIT_BLOCK = Duration.ofHours(1);
    /** 출력 토큰 상한. 요약은 300자 안쪽이라 넉넉하다 */
    static final int MAX_OUTPUT_TOKENS = 1024;
    /** 끝까지 정상으로 썼거나(STOP), 길이 제한에 걸린 경우(MAX_TOKENS, 완성 문장까지만 쓴다)만 쓴다. SAFETY 등 차단은 버린다 */
    private static final Set<String> ACCEPTED_FINISH_REASONS = Set.of("STOP", "MAX_TOKENS");
    /** 모델 이름은 설정값이지만 URL 경로에 들어가므로 형식을 확인한다 */
    private static final Pattern MODEL_NAME = Pattern.compile("^[a-z0-9][a-z0-9.-]*$");
    /** 헤더에 넣을 수 있는 키: 공백 없는 출력 가능한 ASCII */
    private static final Pattern API_KEY = Pattern.compile("^[\\x21-\\x7E]+$");
    /** RetryInfo.retryDelay 형식 (예: "37s", "1.5s") */
    private static final Pattern RETRY_DELAY = Pattern.compile("^(\\d+)(?:\\.\\d+)?s$");

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final HttpClient client;
    private final String apiKey;
    private final String model;
    private final URI baseUri;
    private final Duration timeout;
    private final Clock clock;
    /** 429를 받아 호출을 멈추는 끝 시각. 여러 요약 스레드가 함께 보므로 volatile */
    private volatile Instant blockedUntil = Instant.MIN;

    @Autowired
    public GeminiSummaryGenerator(@Value("${trendizm.gemini.api-key:${GEMINI_API_KEY:}}") String apiKey,
                                  @Value("${trendizm.gemini.model:gemini-3.5-flash-lite}") String model) {
        this(apiKey, model, DEFAULT_BASE_URI, DEFAULT_TIMEOUT, Clock.systemUTC());
    }

    /** 테스트에서 로컬 가짜 서버 주소, 짧은 시간 제한, 시계를 넣을 때 쓴다 */
    GeminiSummaryGenerator(String apiKey, String model, URI baseUri, Duration timeout, Clock clock) {
        if (!MODEL_NAME.matcher(model).matches()) {
            throw new IllegalArgumentException("trendizm.gemini.model 형식이 올바르지 않습니다: " + model);
        }
        this.apiKey = validKey(apiKey);
        this.model = model;
        this.baseUri = baseUri;
        this.timeout = timeout;
        this.clock = clock;
        this.client = HttpClient.newBuilder().connectTimeout(timeout).build();
    }

    private static String validKey(String apiKey) {
        String key = apiKey == null ? "" : apiKey.strip();
        if (!key.isEmpty() && !API_KEY.matcher(key).matches()) {
            // 값은 남기지 않는다
            log.warn("Gemini API 키에 헤더로 보낼 수 없는 문자가 있어 AI 요약을 끕니다. 키 값만 넣었는지 확인하세요");
            return "";
        }
        return key;
    }

    @Override
    public boolean isConfigured() {
        return !apiKey.isEmpty();
    }

    @Override
    public boolean isEnabled() {
        return isConfigured() && !clock.instant().isBefore(blockedUntil);
    }

    @Override
    public Optional<String> summarize(String word, List<SummaryOpinion> opinions) {
        if (!isEnabled()) {
            return Optional.empty();
        }
        CompletableFuture<HttpResponse<String>> future = null;
        try {
            // "./"를 붙인다. 없으면 "gemini-...:generateContent"의 콜론 앞이 URI 스킴(http: 같은)으로 해석된다
            HttpRequest request = HttpRequest.newBuilder(baseUri.resolve("./" + model + ":generateContent"))
                    .timeout(timeout)
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody(word, opinions), StandardCharsets.UTF_8))
                    .build();
            future = client.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            HttpResponse<String> response = future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (response.statusCode() == 429) {
                blockUntilRetry(response.body());
                return Optional.empty();
            }
            if (response.statusCode() != 200) {
                // 응답 본문에는 키가 없지만 길 수 있어서 상태 코드만 남긴다
                log.info("AI 요약 응답 실패 status={} model={}", response.statusCode(), model);
                return Optional.empty();
            }
            return parseText(response.body());
        } catch (TimeoutException e) {
            future.cancel(true); // 연결을 끊고 받던 본문을 버린다
            log.info("AI 요약 시간 초과 model={} timeout={}", model, timeout);
            return Optional.empty();
        } catch (ExecutionException e) {
            log.info("AI 요약 호출 실패 model={}: {}", model, e.getCause() == null ? e.toString() : e.getCause().toString());
            return Optional.empty();
        } catch (JacksonException e) {
            log.info("AI 요약 응답 해석 실패 model={}: {}", model, e.toString());
            return Optional.empty();
        } catch (InterruptedException e) {
            if (future != null) {
                future.cancel(true);
            }
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    /** 429를 받으면 retryDelay(없으면 RATE_LIMIT_BLOCK)만큼 모든 호출을 멈춘다 */
    private void blockUntilRetry(String responseBody) {
        Duration block = retryDelay(responseBody)
                .map(delay -> delay.compareTo(RATE_LIMIT_BLOCK) < 0 ? RATE_LIMIT_BLOCK : delay)
                .map(delay -> delay.compareTo(MAX_RATE_LIMIT_BLOCK) > 0 ? MAX_RATE_LIMIT_BLOCK : delay)
                .orElse(RATE_LIMIT_BLOCK);
        blockedUntil = clock.instant().plus(block);
        log.info("AI 요약 한도 초과(429). {} 동안 모든 요약 호출을 멈춥니다 model={}", block, model);
    }

    /** error.details[]에서 RetryInfo의 retryDelay(예: "37s")를 찾는다. 형식이 다르면 empty */
    static Optional<Duration> retryDelay(String responseBody) {
        try {
            for (JsonNode detail : JSON.readTree(responseBody).path("error").path("details")) {
                if (detail.path("@type").asString("").endsWith("google.rpc.RetryInfo")) {
                    Matcher matcher = RETRY_DELAY.matcher(detail.path("retryDelay").asString(""));
                    if (matcher.matches()) {
                        return Optional.of(Duration.ofSeconds(Long.parseLong(matcher.group(1))));
                    }
                }
            }
        } catch (JacksonException | NumberFormatException e) {
            // 형식이 다르면 기본 시간만큼 멈춘다
        }
        return Optional.empty();
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
        if (summary.isEmpty()) {
            return Optional.empty();
        }
        // 길이 제한으로 끊겼으면 문장 중간에서 끝나지 않게 마지막 완성 문장까지만 쓴다
        return Optional.of("MAX_TOKENS".equals(finishReason) ? SummaryText.endAtSentence(summary) : summary);
    }
}
