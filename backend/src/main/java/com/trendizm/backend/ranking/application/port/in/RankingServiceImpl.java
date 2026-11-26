package com.trendizm.backend.ranking.application.port.in;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.ranking.application.dto.RankingCategoryResponse;
import com.trendizm.backend.ranking.application.dto.RankingImportResult;
import com.trendizm.backend.ranking.application.dto.RankingImportResult.MergedKeyword;
import com.trendizm.backend.ranking.application.dto.RankingImportResult.SkippedRow;
import com.trendizm.backend.ranking.application.dto.RankingImportRow;
import com.trendizm.backend.ranking.application.dto.RankingLinkResponse;
import com.trendizm.backend.ranking.application.dto.RankingResponse;
import com.trendizm.backend.ranking.application.dto.RankingSnapshot;
import com.trendizm.backend.ranking.application.dto.RankingTabs;
import com.trendizm.backend.ranking.application.dto.RankingTabs.PlatformTab;
import com.trendizm.backend.ranking.application.exception.RankingErrorCode;
import com.trendizm.backend.ranking.application.port.RankingService;
import com.trendizm.backend.ranking.application.port.in.RankingImportRules.ParsedLink;
import com.trendizm.backend.ranking.application.port.in.RankingImportRules.ParsedRow;
import com.trendizm.backend.ranking.application.port.in.RankingImportRules.RankedKeyword;
import com.trendizm.backend.ranking.application.port.in.RankingImportRules.RankedSnapshot;
import com.trendizm.backend.ranking.application.port.out.RankingTabRepository;
import com.trendizm.backend.ranking.application.port.out.RankingLinkRepository;
import com.trendizm.backend.ranking.application.port.out.RankingRepository;
import com.trendizm.backend.ranking.domain.entity.TrendRanking;
import com.trendizm.backend.ranking.domain.entity.TrendRankingCategory;
import com.trendizm.backend.ranking.domain.entity.TrendRankingLink;
import com.trendizm.backend.ranking.domain.enums.Platform;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingServiceImpl implements RankingService {

    /** SQLState: UNIQUE 위반 */
    private static final String UNIQUE_VIOLATION = "23505";

    private final RankingRepository rankingRepository;
    private final RankingTabRepository tabRepository;
    private final RankingLinkRepository linkRepository;

    /**
     * 카테고리 slug로 trend_ranking_categories를 찾고, 그 id(trend_rankings.category_id)로 순위를 찾는다.
     * 가장 최근 collectedAt을 찾고, 그 스냅샷의 1~10위와 순위별 링크를 각각 한 번에 가져온다.
     * startedAt은 순위 행마다 저장되지만 한 스냅샷 안에서는 같은 값이라 첫 순위의 값을 쓴다.
     * 미분류 카테고리는 조회 응답에서 빼므로 없는 카테고리와 같이 404다.
     */
    @Override
    public RankingSnapshot getLatestSnapshot(String platformSlug, String categorySlug) {
        Platform platform = Platform.fromSlug(platformSlug)
                .orElseThrow(() -> new BusinessException(RankingErrorCode.RANKING_PLATFORM_NOT_FOUND));
        Long categoryId = tabRepository.findBySlug(categorySlug)
                .filter(category -> !category.isUncategorized())
                .map(TrendRankingCategory::getId)
                .orElseThrow(() -> new BusinessException(RankingErrorCode.RANKING_CATEGORY_NOT_FOUND));

        Optional<TrendRanking> latest =
                rankingRepository.findFirstByPlatformAndCategoryIdOrderByCollectedAtDesc(platform, categoryId);
        if (latest.isEmpty()) {
            return new RankingSnapshot(platform.name(), categoryId, null, null, List.of());
        }

        List<TrendRanking> rankings = rankingRepository.findTop10ByPlatformAndCategoryIdAndCollectedAtOrderByRankAsc(
                platform, categoryId, latest.get().getCollectedAt());
        Map<Long, List<RankingLinkResponse>> linksByRankingId = linkRepository
                .findByRankingIdInOrderBySortOrderAscIdAsc(rankings.stream().map(TrendRanking::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(TrendRankingLink::getRankingId,
                        Collectors.mapping(RankingLinkResponse::from, Collectors.toList())));

        TrendRanking first = rankings.getFirst();
        List<RankingResponse> responses = rankings.stream()
                .map(ranking -> RankingResponse.from(ranking)
                        .withLinks(linksByRankingId.getOrDefault(ranking.getId(), List.of())))
                .toList();
        return new RankingSnapshot(platform.name(), categoryId, first.getStartedAt(), first.getCollectedAt(), responses);
    }

    /** 플랫폼은 enum 선언 순서, 카테고리는 미분류를 빼고 id 순으로 준다 */
    @Override
    public RankingTabs getTabs() {
        List<PlatformTab> platforms = Arrays.stream(Platform.values()).map(PlatformTab::from).toList();
        List<RankingCategoryResponse> categories = tabRepository.findAllByOrderByIdAsc().stream()
                .filter(category -> !category.isUncategorized())
                .map(RankingCategoryResponse::from)
                .toList();
        return new RankingTabs(platforms, categories);
    }

    /**
     * 순위 CSV를 저장한다. 파일 전체가 한 트랜잭션이다.
     * 1. 줄마다 값을 검사한다. 틀린 줄이 있으면 아무것도 저장하지 않고 줄 번호와 이유를 모두 알린다 (RankingImportRules.parse).
     * 2. 카테고리가 DB에 없거나 순위 칸이 빈 줄은 건너뛰고 skipped로 알린다.
     * 3. 플랫폼·카테고리마다 한 스냅샷이다. 같은 수집 시각의 스냅샷이 하나라도 이미 있으면 파일 전체를 거부한다.
     * 4. 스냅샷마다 중복 키워드를 합치고 순위를 다시 매긴 뒤(RankingImportRules.rank) 순위와 관련 링크를 저장한다.
     */
    @Override
    @Transactional
    public RankingImportResult importRankings(List<RankingImportRow> rows) {
        List<ParsedRow> parsed = RankingImportRules.parse(rows);
        LocalDateTime collectedAt = parsed.getFirst().collectedAt();

        Map<String, TrendRankingCategory> categories = tabRepository
                .findByTitleIn(parsed.stream().map(ParsedRow::category).distinct().toList()).stream()
                .collect(Collectors.toMap(TrendRankingCategory::getTitle, Function.identity()));
        List<SkippedRow> skipped = new ArrayList<>();
        Map<SnapshotKey, List<ParsedRow>> snapshots = new LinkedHashMap<>();
        for (ParsedRow row : parsed) {
            TrendRankingCategory category = categories.get(row.category());
            if (category == null) {
                skipped.add(new SkippedRow(row.line(), row.category(), row.keyword(), "카테고리가 없습니다."));
            } else if (row.rank() == null) {
                skipped.add(new SkippedRow(row.line(), row.category(), row.keyword(), "순위가 비어 있습니다."));
            } else {
                snapshots.computeIfAbsent(new SnapshotKey(row.platform(), category.getId(), category.getTitle()),
                        key -> new ArrayList<>()).add(row);
            }
        }

        List<String> existing = snapshots.keySet().stream()
                .filter(key -> rankingRepository.existsByPlatformAndCategoryIdAndCollectedAt(
                        key.platform(), key.categoryId(), collectedAt))
                .map(key -> key.platform().name() + "·" + key.categoryTitle())
                .toList();
        if (!existing.isEmpty()) {
            throw new BusinessException(RankingErrorCode.RANKING_SNAPSHOT_ALREADY_EXISTS,
                    "이미 올린 수집 결과입니다 (collected_at " + collectedAt + "): " + String.join(", ", existing));
        }

        List<MergedKeyword> merged = new ArrayList<>();
        int rankingCount = 0;
        int linkCount = 0;
        try {
            for (Map.Entry<SnapshotKey, List<ParsedRow>> snapshot : snapshots.entrySet()) {
                RankedSnapshot ranked = RankingImportRules.rank(snapshot.getValue());
                merged.addAll(ranked.merged());
                for (RankedKeyword keyword : ranked.rankings()) {
                    ParsedRow row = keyword.row();
                    TrendRanking saved = rankingRepository.save(TrendRanking.create(snapshot.getKey().categoryId(),
                            row.platform(), keyword.rank(), keyword.keyword(), row.startedAt(), row.collectedAt()));
                    rankingCount++;
                    List<ParsedLink> links = row.links();
                    for (int sortOrder = 0; sortOrder < links.size(); sortOrder++) {
                        ParsedLink link = links.get(sortOrder);
                        linkRepository.save(TrendRankingLink.create(
                                saved.getId(), link.title(), link.url(), null, null, null, sortOrder));
                        linkCount++;
                    }
                }
            }
        } catch (DataIntegrityViolationException e) {
            // 같은 파일을 거의 동시에 두 번 올려 위의 확인을 함께 통과하면, 스냅샷 순위 UNIQUE가 막는다.
            // NOT NULL 등 다른 제약 위반은 중복 업로드가 아니므로 그대로 던진다(DB 칼럼이 엔티티와 다를 때 등)
            if (isUniqueViolation(e)) {
                throw new BusinessException(RankingErrorCode.RANKING_SNAPSHOT_ALREADY_EXISTS);
            }
            throw e;
        }
        return new RankingImportResult(collectedAt, rankingCount, linkCount, skipped, merged);
    }

    /**
     * UNIQUE 위반인지. Spring은 UNIQUE와 NOT NULL 위반을 같은 DataIntegrityViolationException으로 바꾸므로
     * 원인 중 JDBC 예외의 SQLState(23505, PostgreSQL·H2 공통)로 구분한다.
     */
    private static boolean isUniqueViolation(DataIntegrityViolationException e) {
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sqlException) {
                return UNIQUE_VIOLATION.equals(sqlException.getSQLState());
            }
        }
        return false;
    }

    /** 플랫폼·카테고리 하나가 한 스냅샷이다(수집 시각은 파일 전체가 같다) */
    private record SnapshotKey(Platform platform, Long categoryId, String categoryTitle) {
    }
}
