package com.trendizm.backend.ranking.adapter.in.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.trendizm.backend.ranking.application.port.out.RankingTabRepository;
import com.trendizm.backend.ranking.domain.entity.TrendRankingCategory;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Python 파이프라인(python/ranking/instagram/instagram_keyword_pipeline.py)이 만든 CSV를 그대로 올려서
 * 저장하고 조회 API로 다시 읽는다. 테스트 파일 ranking/ranking_instagram_sample.csv는 그 스크립트의 save_csv로 만든 것이다
 * (UTF-8 BOM, JSON 배열 칸, 순위가 빈 줄, 255자로 자른 캡션, 따옴표가 들어간 제목 포함).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RankingCsvUploadTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RankingTabRepository tabRepository;

    private Long challengeId;

    @BeforeEach
    void setUp() {
        challengeId = tabRepository.save(TrendRankingCategory.create("challenge", "챌린지")).getId();
        tabRepository.save(TrendRankingCategory.create("meme", "밈"));
    }

    @Test
    @DisplayName("Python이 만든 CSV를 올리면 순위와 관련 링크를 저장하고, 조회 API로 같은 내용을 준다. 같은 파일을 다시 올리면 409")
    void 업로드_후_조회() throws Exception {
        byte[] csv = sample();

        mockMvc.perform(post("/api/admin/rankings").contentType(MediaType.parseMediaType("text/csv")).content(csv)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.collectedAt").value("2026-11-23T12:16:00"))
                .andExpect(jsonPath("$.savedRankingCount").value(1))
                .andExpect(jsonPath("$.savedLinkCount").value(4))
                .andExpect(jsonPath("$.skipped[0].line").value(3))
                .andExpect(jsonPath("$.skipped[0].reason").value("순위가 비어 있습니다."));

        mockMvc.perform(get("/api/rankings").param("platform", "instagram").param("category", "challenge"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(challengeId))
                .andExpect(jsonPath("$.startedAt").value("2026-11-16T12:04:14"))
                .andExpect(jsonPath("$.collectedAt").value("2026-11-23T12:16:00"))
                .andExpect(jsonPath("$.rankings[0].rank").value(1))
                .andExpect(jsonPath("$.rankings[0].keyword").value("곶감챌린지"))
                .andExpect(jsonPath("$.rankings[0].links.length()").value(4))
                .andExpect(jsonPath("$.rankings[0].links[0].url").value("https://www.instagram.com/p/CollectedA1/"))
                .andExpect(jsonPath("$.rankings[0].links[1].title").isEmpty())
                .andExpect(jsonPath("$.rankings[0].links[2].url").value("https://www.instagram.com/p/GoogleReel1/"))
                .andExpect(jsonPath("$.rankings[0].links[2].title").value("김댄서 on Instagram: \"곶감 챌린지\""));

        mockMvc.perform(post("/api/admin/rankings").contentType(MediaType.parseMediaType("text/csv")).content(csv)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RANKING_SNAPSHOT_ALREADY_EXISTS"));
    }

    private static byte[] sample() throws Exception {
        try (InputStream in = RankingCsvUploadTest.class.getResourceAsStream("/ranking/ranking_instagram_sample.csv")) {
            return in.readAllBytes();
        }
    }
}
