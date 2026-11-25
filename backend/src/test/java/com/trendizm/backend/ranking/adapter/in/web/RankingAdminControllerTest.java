package com.trendizm.backend.ranking.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.trendizm.backend.auth.adapter.out.security.SecurityConfig;
import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.ranking.application.dto.RankingImportResult;
import com.trendizm.backend.ranking.application.dto.RankingImportRow;
import com.trendizm.backend.ranking.application.dto.RankingSummaryResult;
import com.trendizm.backend.ranking.application.exception.RankingErrorCode;
import com.trendizm.backend.ranking.application.port.RankingService;
import com.trendizm.backend.ranking.application.port.RankingSummaryService;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(RankingAdminController.class)
@Import(SecurityConfig.class)
class RankingAdminControllerTest {

    private static final MediaType TEXT_CSV = MediaType.parseMediaType("text/csv");

    private static final String HEADER = "platform,category,ranking,keyword,count,related_posts,related_posts_author,"
            + "related_posts_title,collected_at,started_at\n";

    private static final SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor ADMIN =
            user("admin").roles("ADMIN");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RankingService rankingService;

    @MockitoBean
    private RankingSummaryService summaryService;

    @Test
    @DisplayName("관리자가 올린 CSV를 줄마다 읽어 넘긴다. BOM은 지우고, 따옴표 안의 줄바꿈·쉼표와 배열 안의 null도 읽는다")
    void 업로드() throws Exception {
        String csv = "\uFEFF" + HEADER
                // 2~3번째 줄: 키워드 칸 안에 줄바꿈. 4번째 줄: 빈 줄. 5번째 줄: 둘째 행
                // 제목 배열 안의 줄바꿈은 Python json.dumps처럼 \n으로 이스케이프한다 (JSON 문자열에는 줄바꿈을 그대로 쓸 수 없다)
                + "instagram,챌린지,1,\"copy\npaste\",18100,\"[\"\"Da0E9_oBZM-\"\", \"\"DZrXQRiSv6q\"\"]\","
                + "\"[\"\"user_a\"\", null]\",\"[\"\"첫 줄\\n둘째 줄\"\", \"\"제목, 쉼표\"\"]\",2026-11-23 12:16,2026-11-16 12:04:14\n"
                + "\n"
                + "instagram,밈,1,l을가져가,8230,\"[\"\"Dd9DDsuiq7u\"\"]\",[],,2026-11-23 12:16,2026-11-16 12:04:14\n";
        given(rankingService.importRankings(any())).willReturn(new RankingImportResult(
                LocalDateTime.of(2026, 11, 23, 12, 16), 2, 3, List.of(), List.of()));

        mockMvc.perform(upload(csv.getBytes(StandardCharsets.UTF_8)).with(ADMIN))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.collectedAt").value("2026-11-23T12:16:00"))
                .andExpect(jsonPath("$.savedRankingCount").value(2));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RankingImportRow>> captor = ArgumentCaptor.forClass(List.class);
        verify(rankingService).importRankings(captor.capture());
        List<RankingImportRow> rows = captor.getValue();
        assertThat(rows).extracting(RankingImportRow::line).containsExactly(2, 5);
        RankingImportRow first = rows.getFirst();
        assertThat(first.platform()).isEqualTo("instagram");
        assertThat(first.category()).isEqualTo("챌린지");
        assertThat(first.keyword()).isEqualTo("copy\npaste");
        assertThat(first.postIds()).containsExactly("Da0E9_oBZM-", "DZrXQRiSv6q");
        assertThat(first.postAuthors()).containsExactly("user_a", null);
        assertThat(first.postTitles()).containsExactly("첫 줄\n둘째 줄", "제목, 쉼표");
        assertThat(first.collectedAt()).isEqualTo("2026-11-23 12:16");
        RankingImportRow second = rows.get(1);
        assertThat(second.postAuthors()).isEmpty();
        assertThat(second.postTitles()).isEmpty();
    }

    @Test
    @DisplayName("JSON 배열 칸을 따옴표로 감싸지 않아 CSV 문법이 틀리면 그 줄 번호로 400 RANKING_CSV_INVALID")
    void CSV_문법_오류() throws Exception {
        String csv = HEADER
                + "instagram,챌린지,1,곶감,10,\"[\"\"DfZfEqSzZKJ\"\"]\",[],[],2026-11-23 12:16,\n"
                + "instagram,챌린지,2,박스먹방,444,[\"\"DeOLrq_IAU1\"\",\"\"DeJPVa7h0JQ\"\"],[],[],2026-11-23 12:16,\n";

        mockMvc.perform(upload(csv.getBytes(StandardCharsets.UTF_8)).with(ADMIN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RANKING_CSV_INVALID"))
                .andExpect(jsonPath("$.errors[0].field").value("3번째 줄"));

        verifyNoInteractions(rankingService);
    }

    @Test
    @DisplayName("열 개수가 헤더와 다른 줄과 JSON 배열이 아닌 칸은 모두 모아 알린다")
    void 열_오류() throws Exception {
        String csv = HEADER
                + "instagram,챌린지,1,곶감,10,[],[],[],2026-11-23 12:16\n"
                + "instagram,챌린지,2,박스먹방,444,Dd9DDsuiq7u,[],[],2026-11-23 12:16,\n";

        mockMvc.perform(upload(csv.getBytes(StandardCharsets.UTF_8)).with(ADMIN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(2))
                .andExpect(jsonPath("$.errors[0].field").value("2번째 줄"))
                .andExpect(jsonPath("$.errors[1].field").value("3번째 줄"))
                .andExpect(jsonPath("$.errors[1].message").value(org.hamcrest.Matchers.startsWith("related_posts:")));

        verifyNoInteractions(rankingService);
    }

    @Test
    @DisplayName("헤더에 필요한 열이 없으면 400으로 없는 열을 알린다")
    void 헤더_오류() throws Exception {
        String csv = "platform,category,ranking,keyword,count,related_posts\n"
                + "instagram,챌린지,1,곶감,10,[]\n";

        mockMvc.perform(upload(csv.getBytes(StandardCharsets.UTF_8)).with(ADMIN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("1번째 줄"))
                .andExpect(jsonPath("$.errors[0].message").value(
                        "헤더에 없는 열이 있습니다: related_posts_author, related_posts_title, collected_at, started_at"));
    }

    @Test
    @DisplayName("UTF-8이 아닌 파일(엑셀 기본 CP949 등)은 400으로 거부한다")
    void UTF8이_아님() throws Exception {
        byte[] eucKr = (HEADER + "instagram,챌린지,1,곶감,10,[],[],[],2026-11-23 12:16,\n").getBytes(Charset.forName("EUC-KR"));

        mockMvc.perform(upload(eucKr).with(ADMIN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RANKING_CSV_INVALID"))
                .andExpect(jsonPath("$.errors[0].message").value(org.hamcrest.Matchers.startsWith("UTF-8로 저장된 CSV가 아닙니다.")));

        verifyNoInteractions(rankingService);
    }

    @Test
    @DisplayName("5MB를 넘으면 413 RANKING_CSV_TOO_LARGE")
    void 너무_큼() throws Exception {
        byte[] body = new byte[RankingCsvReader.MAX_BYTES + 1];
        Arrays.fill(body, (byte) 'a');

        mockMvc.perform(upload(body).with(ADMIN))
                .andExpect(status().is(HttpStatus.CONTENT_TOO_LARGE.value()))
                .andExpect(jsonPath("$.code").value("RANKING_CSV_TOO_LARGE"));
    }

    @Test
    @DisplayName("text/csv가 아니면 415로 거부한다 (multipart 업로드도 받지 않는다)")
    void 다른_형식() throws Exception {
        mockMvc.perform(post("/api/admin/rankings").contentType(MediaType.APPLICATION_JSON).content("{}").with(ADMIN))
                .andExpect(status().isUnsupportedMediaType());
        mockMvc.perform(post("/api/admin/rankings").contentType(MediaType.MULTIPART_FORM_DATA).content("x").with(ADMIN))
                .andExpect(status().isUnsupportedMediaType());

        verifyNoInteractions(rankingService);
    }

    @Test
    @DisplayName("로그인하지 않으면 401, 관리자가 아니면 403")
    void 관리자만() throws Exception {
        byte[] body = HEADER.getBytes(StandardCharsets.UTF_8);

        mockMvc.perform(upload(body)).andExpect(status().isUnauthorized());
        mockMvc.perform(upload(body).with(user("member").roles("USER"))).andExpect(status().isForbidden());

        verifyNoInteractions(rankingService);
    }

    @Test
    @DisplayName("AI 요약: 플랫폼·카테고리를 넘기고 결과를 준다. overwrite 기본값은 false")
    void 요약() throws Exception {
        given(summaryService.summarizeLatest("instagram", "challenge", false)).willReturn(new RankingSummaryResult(
                "instagram", 1L, LocalDateTime.of(2026, 11, 23, 12, 16),
                List.of(new RankingSummaryResult.SummarizedKeyword(7L, 1, "걸그립", "소지품을 쥐는 챌린지다.")),
                List.of(new RankingSummaryResult.SkippedKeyword(8L, 2, "춘배", "이미 요약이 있습니다."))));

        mockMvc.perform(summaries().with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summarized[0].summary").value("소지품을 쥐는 챌린지다."))
                .andExpect(jsonPath("$.skipped[0].reason").value("이미 요약이 있습니다."));

        mockMvc.perform(summaries().param("overwrite", "true").with(ADMIN)).andExpect(status().isOk());
        verify(summaryService).summarizeLatest("instagram", "challenge", true);
    }

    @Test
    @DisplayName("AI 요약: 파라미터가 없으면 400, AI를 쓸 수 없으면 503, 로그인하지 않으면 401, 관리자가 아니면 403")
    void 요약_오류() throws Exception {
        mockMvc.perform(post("/api/admin/rankings/summaries").with(ADMIN)).andExpect(status().isBadRequest());
        mockMvc.perform(summaries()).andExpect(status().isUnauthorized());
        mockMvc.perform(summaries().with(user("member").roles("USER"))).andExpect(status().isForbidden());
        verifyNoInteractions(summaryService);

        given(summaryService.summarizeLatest(any(), any(), org.mockito.ArgumentMatchers.anyBoolean()))
                .willThrow(new BusinessException(RankingErrorCode.RANKING_SUMMARY_NOT_CONFIGURED));
        mockMvc.perform(summaries().with(ADMIN))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("RANKING_SUMMARY_NOT_CONFIGURED"));
    }

    private static MockHttpServletRequestBuilder summaries() {
        return post("/api/admin/rankings/summaries").param("platform", "instagram").param("category", "challenge");
    }

    private static MockHttpServletRequestBuilder upload(byte[] body) {
        return post("/api/admin/rankings").contentType(TEXT_CSV).content(body);
    }
}
