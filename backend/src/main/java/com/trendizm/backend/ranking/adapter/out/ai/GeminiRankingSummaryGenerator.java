package com.trendizm.backend.ranking.adapter.out.ai;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.ranking.application.exception.RankingErrorCode;
import com.trendizm.backend.ranking.application.port.out.RankingSummaryGenerator;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Gemini API(generateContent)로 순위 키워드를 요약한다. SDK 없이 JDK HttpClient로 REST를 부른다(의존성 추가 없음).
 * - API 키: trendizm.gemini.api-key, 없으면 환경 변수·.env의 GEMINI_API_KEY. URL이 아니라 헤더로 보내고 로그에 남기지 않는다.
 *   헤더에 넣을 수 없는 문자가 섞이면 JDK가 키 값을 담은 예외를 던지므로, 키가 없는 것으로 본다.
 * - 시간 제한: HttpRequest.timeout은 응답 헤더까지만 적용되므로, 본문을 다 받을 때까지 기다리는 시간을 따로 제한한다.
 * - 재시도는 하지 않는다. 관리자가 다시 부르면 된다.
 */
@Slf4j
@Component
public class GeminiRankingSummaryGenerator implements RankingSummaryGenerator {

    static final String MODEL = "gemini-3.5-flash-lite";
    static final URI DEFAULT_BASE_URI = URI.create("https://generativelanguage.googleapis.com/v1beta/models/");
    static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(60);
    /** 키워드 10개의 요약(300자 이내)과 인용문이 들어가는 크기 */
    static final int MAX_OUTPUT_TOKENS = 8192;

    /** 헤더에 넣을 수 있는 키: 공백 없는 출력 가능한 ASCII */
    private static final Pattern API_KEY = Pattern.compile("^[\\x21-\\x7E]+$");

    private static final Map<String, Object> RESPONSE_SCHEMA = Map.of(
            "type", "object",
            "properties", Map.of("items", Map.of(
                    "type", "array",
                    "items", Map.of(
                            "type", "object",
                            "properties", Map.of(
                                    "id", Map.of("type", "integer"),
                                    "summary", Map.of("type", "string"),
                                    "evidence", Map.of("type", "string")),
                            "required", List.of("id", "summary", "evidence")))),
            "required", List.of("items"));

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final HttpClient client;
    private final String apiKey;
    private final URI baseUri;
    private final Duration timeout;

    @Autowired
    public GeminiRankingSummaryGenerator(@Value("${trendizm.gemini.api-key:${GEMINI_API_KEY:}}") String apiKey) {
        this(apiKey, DEFAULT_BASE_URI, DEFAULT_TIMEOUT);
    }

    /** 테스트에서 로컬 가짜 서버 주소와 짧은 시간 제한을 넣을 때 쓴다 */
    GeminiRankingSummaryGenerator(String apiKey, URI baseUri, Duration timeout) {
        this.apiKey = apiKey == null ? "" : apiKey.strip();
        this.baseUri = baseUri;
        this.timeout = timeout;
        this.client = HttpClient.newBuilder().connectTimeout(timeout).build();
    }

    @Override
    public List<Answer> summarize(List<Target> targets) {
        if (!API_KEY.matcher(apiKey).matches()) {
            throw new BusinessException(RankingErrorCode.RANKING_SUMMARY_NOT_CONFIGURED);
        }
        HttpResponse<String> response = send(requestBody(targets));
        if (response.statusCode() == 429) {
            throw new BusinessException(RankingErrorCode.RANKING_SUMMARY_RATE_LIMITED);
        }
        if (response.statusCode() != 200) {
            throw failed("응답 상태 " + response.statusCode());
        }
        try {
            return parse(response.body(), targets);
        } catch (JacksonException e) {
            throw failed("응답이 JSON이 아님");
        }
    }

    private HttpResponse<String> send(String body) {
        // "./"를 붙인다. 없으면 "gemini-...:generateContent"의 콜론 앞이 URI 스킴(http: 같은)으로 해석된다
        HttpRequest request = HttpRequest.newBuilder(baseUri.resolve("./" + MODEL + ":generateContent"))
                .timeout(timeout)
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        CompletableFuture<HttpResponse<String>> future =
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        try {
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true); // 연결을 끊고 받던 본문을 버린다
            throw failed("시간 초과 " + timeout);
        } catch (ExecutionException e) {
            throw failed(String.valueOf(e.getCause()));
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw failed("중단됨");
        }
    }

    static String requestBody(List<Target> targets) {
        return JSON.writeValueAsString(Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", RankingSummaryPrompt.INSTRUCTION))),
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", RankingSummaryPrompt.userMessage(targets))))),
                "generationConfig", Map.of(
                        "maxOutputTokens", MAX_OUTPUT_TOKENS,
                        "responseMimeType", "application/json",
                        "responseJsonSchema", RESPONSE_SCHEMA)));
    }

    /** 끝까지 쓴 응답(STOP)만 쓴다. 생각(thought) 파트를 뺀 글이 JSON이고, 보낸 키워드마다 답이 하나씩 있어야 한다 */
    private static List<Answer> parse(String responseBody, List<Target> targets) {
        JsonNode root = JSON.readTree(responseBody);
        JsonNode candidate = root.path("candidates").path(0);
        String finishReason = candidate.path("finishReason").asString("");
        if (!"STOP".equals(finishReason)) {
            throw failed("finishReason=" + finishReason
                    + " blockReason=" + root.path("promptFeedback").path("blockReason").asString(""));
        }
        StringBuilder text = new StringBuilder();
        for (JsonNode part : candidate.path("content").path("parts")) {
            if (!part.path("thought").asBoolean(false)) {
                text.append(part.path("text").asString(""));
            }
        }

        Set<Long> expected = targets.stream().map(Target::rankingId).collect(Collectors.toSet());
        Set<Long> answered = new HashSet<>();
        List<Answer> answers = new ArrayList<>();
        for (JsonNode item : JSON.readTree(text.toString()).path("items")) {
            long id = item.path("id").asLong(-1);
            if (!expected.contains(id) || !answered.add(id)) {
                throw failed("보내지 않았거나 중복된 id");
            }
            answers.add(new Answer(id, item.path("summary").asString(""), item.path("evidence").asString("")));
        }
        if (!answered.equals(expected)) {
            throw failed("빠진 id가 있음");
        }
        return answers;
    }

    /** 응답 본문과 키는 남기지 않고 이유만 남긴다 */
    private static BusinessException failed(String reason) {
        log.warn("순위 AI 요약 실패 model={}: {}", MODEL, reason);
        return new BusinessException(RankingErrorCode.RANKING_SUMMARY_FAILED);
    }
}
