package com.trendizm.backend.ranking.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.global.error.ErrorResponse;
import com.trendizm.backend.ranking.application.dto.RankingImportResult;
import com.trendizm.backend.ranking.application.dto.RankingImportResult.MergedKeyword;
import com.trendizm.backend.ranking.application.dto.RankingImportResult.SkippedRow;
import com.trendizm.backend.ranking.application.dto.RankingImportRow;
import com.trendizm.backend.ranking.application.dto.RankingLinkResponse;
import com.trendizm.backend.ranking.application.dto.RankingResponse;
import com.trendizm.backend.ranking.application.dto.RankingSnapshot;
import com.trendizm.backend.ranking.application.exception.RankingErrorCode;
import com.trendizm.backend.ranking.application.port.RankingService;
import com.trendizm.backend.ranking.application.port.out.RankingTabRepository;
import com.trendizm.backend.ranking.domain.entity.TrendRankingCategory;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/** 순위 CSV 업로드(importRankings). 줄은 어댑터가 CSV를 읽어 만든 RankingImportRow를 직접 만들어 넘긴다 */
@SpringBootTest
@Transactional
class RankingImportServiceTest {

    private static final String COLLECTED_AT = "2026-11-23 12:16";
    private static final String STARTED_AT = "2026-11-16 12:04:14";

    @Autowired
    private RankingService rankingService;

    @Autowired
    private RankingTabRepository tabRepository;

    /** 카테고리 챌린지(challenge), 밈(meme), 미분류(uncategorized). 테스트가 끝나면 롤백된다 */
    @BeforeEach
    void setUp() {
        tabRepository.save(TrendRankingCategory.create("challenge", "챌린지"));
        tabRepository.save(TrendRankingCategory.create("meme", "밈"));
        tabRepository.save(TrendRankingCategory.create("uncategorized", "미분류"));
    }

    @Test
    @DisplayName("카테고리마다 스냅샷으로 저장하고, 관련 게시물 ID로 주소를 만들어 제목과 함께 순서대로 저장한다")
    void 저장() {
        RankingImportResult result = rankingService.importRankings(List.of(
                row(2, "챌린지", "1", "copypaste", List.of("Da0E9_oBZM-", "DZrXQRiSv6q"),
                        List.of("user_a", "user.b"), Arrays.asList("첫 게시물", null)),
                row(3, "챌린지", "2", "이두근하트", List.of(), List.of(), List.of()),
                row(4, "밈", "1", "l을가져가", List.of("Dd9DDsuiq7u"), List.of(), List.of())));

        assertThat(result.collectedAt()).isEqualTo(LocalDateTime.of(2026, 11, 23, 12, 16));
        assertThat(result.savedRankingCount()).isEqualTo(3);
        assertThat(result.savedLinkCount()).isEqualTo(3);
        assertThat(result.skipped()).isEmpty();
        assertThat(result.merged()).isEmpty();

        RankingSnapshot challenge = rankingService.getLatestSnapshot("instagram", "challenge");
        assertThat(challenge.collectedAt()).isEqualTo(LocalDateTime.of(2026, 11, 23, 12, 16));
        assertThat(challenge.startedAt()).isEqualTo(LocalDateTime.of(2026, 11, 16, 12, 4, 14));
        assertThat(challenge.rankings()).extracting(RankingResponse::rank, RankingResponse::keyword)
                .containsExactly(tuple(1, "copypaste"), tuple(2, "이두근하트"));
        assertThat(challenge.rankings().getFirst().links())
                .extracting(RankingLinkResponse::url, RankingLinkResponse::title)
                .containsExactly(
                        tuple("https://www.instagram.com/p/Da0E9_oBZM-/", "첫 게시물"),
                        tuple("https://www.instagram.com/p/DZrXQRiSv6q/", null));
        assertThat(rankingService.getLatestSnapshot("instagram", "meme").rankings())
                .extracting(RankingResponse::keyword).containsExactly("l을가져가");
    }

    @Test
    @DisplayName("DB에 없는 카테고리와 순위가 빈 줄은 건너뛰고 이유를 알린다. 미분류는 저장한다")
    void 건너뛴_줄() {
        RankingImportResult result = rankingService.importRankings(List.of(
                row(2, "챌린지", "1", "곶감", List.of(), List.of(), List.of()),
                row(3, "새카테고리", "1", "키워드", List.of(), List.of(), List.of()),
                row(4, "챌린지", "", "순위없음", List.of(), List.of(), List.of()),
                row(5, "미분류", "1", "분류못함", List.of(), List.of(), List.of())));

        assertThat(result.savedRankingCount()).isEqualTo(2);
        assertThat(result.skipped()).extracting(SkippedRow::line, SkippedRow::category, SkippedRow::reason)
                .containsExactly(tuple(3, "새카테고리", "카테고리가 없습니다."), tuple(4, "챌린지", "순위가 비어 있습니다."));
    }

    @Test
    @DisplayName("대소문자만 다른 키워드는 하나로 합친다. 영문자가 4개 이하면 대문자, 5개 이상이면 소문자로 저장한다")
    void 대소문자_중복() {
        RankingImportResult result = rankingService.importRankings(List.of(
                row(2, "챌린지", "1", "CopyPaste", List.of(), List.of(), List.of()),
                row(3, "챌린지", "1", "copypaste", List.of(), List.of(), List.of()),
                row(4, "챌린지", "2", "l을가져가", List.of("Dd9DDsuiq7u"), List.of(), List.of()),
                row(5, "챌린지", "2", "L을가져가", List.of("DeeYEKmTu20"), List.of(), List.of()),
                row(6, "밈", "1", "L을가져가", List.of(), List.of(), List.of())));

        RankingSnapshot challenge = rankingService.getLatestSnapshot("instagram", "challenge");
        assertThat(challenge.rankings()).extracting(RankingResponse::rank, RankingResponse::keyword)
                .containsExactly(tuple(1, "copypaste"), tuple(2, "L을가져가"));
        // 남긴 줄(위쪽 줄)의 관련 게시물을 쓴다
        assertThat(challenge.rankings().get(1).links()).extracting(RankingLinkResponse::url)
                .containsExactly("https://www.instagram.com/p/Dd9DDsuiq7u/");
        // 중복이 없는 키워드는 그대로 둔다
        assertThat(rankingService.getLatestSnapshot("instagram", "meme").rankings())
                .extracting(RankingResponse::keyword).containsExactly("L을가져가");
        assertThat(result.merged()).extracting(MergedKeyword::category, MergedKeyword::keyword, MergedKeyword::lines)
                .containsExactly(tuple("챌린지", "copypaste", List.of(2, 3)), tuple("챌린지", "L을가져가", List.of(4, 5)));
    }

    @Test
    @DisplayName("순위가 다른 중복은 높은 순위만 남기고 뒤 순위를 당긴다")
    void 순위가_다른_중복() {
        rankingService.importRankings(List.of(
                row(2, "챌린지", "1", "곶감", List.of(), List.of(), List.of()),
                row(3, "챌린지", "2", "TMI", List.of(), List.of(), List.of()),
                row(4, "챌린지", "3", "박스먹방", List.of(), List.of(), List.of()),
                row(5, "챌린지", "4", "tmi", List.of(), List.of(), List.of()),
                row(6, "챌린지", "5", "바보털", List.of(), List.of(), List.of())));

        assertThat(rankingService.getLatestSnapshot("instagram", "challenge").rankings())
                .extracting(RankingResponse::rank, RankingResponse::keyword)
                .containsExactly(tuple(1, "곶감"), tuple(2, "TMI"), tuple(3, "박스먹방"), tuple(4, "바보털"));
    }

    @Test
    @DisplayName("완전히 같은 키워드는 위쪽 줄만 남기고, 같은 순위의 서로 다른 키워드는 위쪽 줄을 앞 순위로 매긴다")
    void 같은_순위() {
        RankingImportResult result = rankingService.importRankings(List.of(
                row(2, "챌린지", "1", "곶감", List.of("DfZfEqSzZKJ"), List.of(), List.of()),
                row(3, "챌린지", "1", "곶감", List.of("DgCvSsmHzai"), List.of(), List.of()),
                row(4, "챌린지", "1", "박스먹방", List.of(), List.of(), List.of()),
                row(5, "챌린지", "2", "바보털", List.of(), List.of(), List.of())));

        RankingSnapshot snapshot = rankingService.getLatestSnapshot("instagram", "challenge");
        assertThat(snapshot.rankings()).extracting(RankingResponse::rank, RankingResponse::keyword)
                .containsExactly(tuple(1, "곶감"), tuple(2, "박스먹방"), tuple(3, "바보털"));
        assertThat(snapshot.rankings().getFirst().links()).extracting(RankingLinkResponse::url)
                .containsExactly("https://www.instagram.com/p/DfZfEqSzZKJ/");
        assertThat(result.merged()).extracting(MergedKeyword::keyword, MergedKeyword::lines)
                .containsExactly(tuple("곶감", List.of(2, 3)));
    }

    @Test
    @DisplayName("같은 수집 시각의 스냅샷을 다시 올리면 RANKING_SNAPSHOT_ALREADY_EXISTS")
    void 다시_올리기() {
        List<RankingImportRow> rows = List.of(row(2, "챌린지", "1", "곶감", List.of(), List.of(), List.of()));
        rankingService.importRankings(rows);

        assertThatThrownBy(() -> rankingService.importRankings(rows))
                .isInstanceOfSatisfying(BusinessException.class, e -> {
                    assertThat(e.getErrorCode()).isEqualTo(RankingErrorCode.RANKING_SNAPSHOT_ALREADY_EXISTS);
                    assertThat(e.getMessage()).contains("instagram·챌린지");
                });
    }

    @Test
    @DisplayName("틀린 줄은 모두 모아 줄 번호와 이유를 알리고, 아무것도 저장하지 않는다")
    void 틀린_줄() {
        assertThatThrownBy(() -> rankingService.importRankings(List.of(
                new RankingImportRow(2, "naver", "챌린지", "1", "곶감", List.of(), List.of(), List.of(), COLLECTED_AT, ""),
                row(3, "챌린지", "0", "박스먹방", List.of(), List.of(), List.of()),
                row(4, "챌린지", "2", "바보털", List.of("bad/id"), List.of(), List.of()),
                row(5, "챌린지", "3", "고양이챌린지", List.of("Df2WcyeMtUc"), List.of("a", "b"), List.of()),
                new RankingImportRow(6, "instagram", "챌린지", "4", "", List.of(), List.of(), List.of(), "어제", ""))))
                .isInstanceOfSatisfying(BusinessException.class, e -> {
                    assertThat(e.getErrorCode()).isEqualTo(RankingErrorCode.RANKING_CSV_INVALID);
                    assertThat(e.getErrors()).extracting(ErrorResponse.FieldError::field)
                            .containsExactly("2번째 줄", "3번째 줄", "4번째 줄", "5번째 줄", "6번째 줄", "6번째 줄");
                    assertThat(e.getErrors()).extracting(ErrorResponse.FieldError::message)
                            .anySatisfy(message -> assertThat(message).startsWith("platform:"))
                            .anySatisfy(message -> assertThat(message).startsWith("ranking:"))
                            .anySatisfy(message -> assertThat(message).startsWith("related_posts 1번째:"))
                            .anySatisfy(message -> assertThat(message).startsWith("related_posts_author:"))
                            .anySatisfy(message -> assertThat(message).startsWith("keyword:"))
                            .anySatisfy(message -> assertThat(message).startsWith("collected_at:"));
                });
        assertThat(rankingService.getLatestSnapshot("instagram", "challenge").rankings()).isEmpty();
    }

    @Test
    @DisplayName("category·keyword의 제어 문자는 지우고 저장한다. 줄바꿈·탭은 공백 하나로 바꾸고, 앞뒤 공백은 지운다")
    void 제어_문자가_있는_키워드() {
        RankingImportResult result = rankingService.importRankings(List.of(
                row(2, "챌린지", "1", "곶감\u0000", List.of(), List.of(), List.of()),
                row(3, "챌린지\u0000", "2", "\u0000 박스\u0085먹방 \u0007", List.of(), List.of(), List.of()),
                row(4, "\n챌린지", "3", "copy\r\n\u0000\tpaste\n", List.of(), List.of(), List.of())));

        assertThat(result.skipped()).isEmpty();
        assertThat(rankingService.getLatestSnapshot("instagram", "challenge").rankings())
                .extracting(RankingResponse::rank, RankingResponse::keyword)
                .containsExactly(tuple(1, "곶감"), tuple(2, "박스먹방"), tuple(3, "copy paste"));
    }

    @Test
    @DisplayName("제어 문자를 지운 뒤에 같은 키워드면 합치고, 길이도 지운 뒤에 센다")
    void 제어_문자를_지운_뒤_비교() {
        String longest = "가".repeat(255);
        RankingImportResult result = rankingService.importRankings(List.of(
                row(2, "챌린지", "1", "곶감\u0000", List.of(), List.of(), List.of()),
                row(3, "챌린지", "2", "곶감", List.of(), List.of(), List.of()),
                row(4, "챌린지", "3", longest + "\u0000", List.of(), List.of(), List.of())));

        assertThat(rankingService.getLatestSnapshot("instagram", "challenge").rankings())
                .extracting(RankingResponse::rank, RankingResponse::keyword)
                .containsExactly(tuple(1, "곶감"), tuple(2, longest));
        assertThat(result.merged()).extracting(MergedKeyword::keyword, MergedKeyword::lines)
                .containsExactly(tuple("곶감", List.of(2, 3)));
    }

    @Test
    @DisplayName("category·keyword가 제어 문자뿐이면 빈 칸과 같이 줄 번호와 함께 알린다")
    void 제어_문자뿐인_키워드() {
        assertThatThrownBy(() -> rankingService.importRankings(List.of(
                row(2, "챌린지", "1", "\u0000", List.of(), List.of(), List.of()),
                row(3, " \u0000 ", "2", "곶감", List.of(), List.of(), List.of()))))
                .isInstanceOfSatisfying(BusinessException.class, e -> {
                    assertThat(e.getErrorCode()).isEqualTo(RankingErrorCode.RANKING_CSV_INVALID);
                    assertThat(e.getErrors()).extracting(ErrorResponse.FieldError::field, ErrorResponse.FieldError::message)
                            .containsExactly(tuple("2번째 줄", "keyword: 비어 있습니다."),
                                    tuple("3번째 줄", "category: 비어 있습니다."));
                });
    }

    @Test
    @DisplayName("관련 게시물 제목의 제어 문자는 지우고 저장한다")
    void 제어_문자가_있는_제목() {
        rankingService.importRankings(List.of(row(2, "챌린지", "1", "곶감", List.of("Da0E9_oBZM-", "DZrXQRiSv6q"),
                List.of(), List.of("곶감\u0000 챌린지", "\u0000"))));

        assertThat(rankingService.getLatestSnapshot("instagram", "challenge").rankings().getFirst().links())
                .extracting(RankingLinkResponse::title).containsExactly("곶감 챌린지", null);
    }

    @Test
    @DisplayName("한 파일의 collected_at·started_at은 모든 줄이 같아야 한다")
    void 수집_시각이_다른_줄() {
        assertThatThrownBy(() -> rankingService.importRankings(List.of(
                row(2, "챌린지", "1", "곶감", List.of(), List.of(), List.of()),
                new RankingImportRow(3, "instagram", "밈", "1", "밈 키워드", List.of(), List.of(), List.of(),
                        "2026-11-23 12:17", STARTED_AT))))
                .isInstanceOfSatisfying(BusinessException.class, e -> {
                    assertThat(e.getErrors()).extracting(ErrorResponse.FieldError::field).containsExactly("3번째 줄");
                    assertThat(e.getErrors().getFirst().message()).startsWith("collected_at이 2번째 줄");
                });
    }

    @Test
    @DisplayName("threads·tiktok은 작성자가 있어야 주소를 만들 수 있다")
    void 작성자가_필요한_플랫폼() {
        assertThatThrownBy(() -> rankingService.importRankings(List.of(new RankingImportRow(2, "tiktok", "챌린지", "1",
                "곶감", List.of("7312345678901234567"), List.of(), List.of(), COLLECTED_AT, STARTED_AT))))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrors()).extracting(ErrorResponse.FieldError::message)
                                .singleElement().asString().startsWith("related_posts_author 1번째:"));

        rankingService.importRankings(List.of(new RankingImportRow(2, "tiktok", "챌린지", "1", "곶감",
                List.of("7312345678901234567"), List.of("dancer.kim"), List.of(), COLLECTED_AT, STARTED_AT)));

        assertThat(rankingService.getLatestSnapshot("tiktok", "challenge").rankings().getFirst().links())
                .extracting(RankingLinkResponse::url)
                .containsExactly("https://www.tiktok.com/@dancer.kim/video/7312345678901234567");
    }

    private static RankingImportRow row(int line, String category, String ranking, String keyword,
                                        List<String> ids, List<String> authors, List<String> titles) {
        return new RankingImportRow(line, "instagram", category, ranking, keyword, ids, authors, titles,
                COLLECTED_AT, STARTED_AT);
    }
}
