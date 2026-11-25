package com.trendizm.backend.ranking.application.port.in;

import com.trendizm.backend.ranking.application.dto.RankingLinkResponse;
import com.trendizm.backend.ranking.application.dto.RankingResponse;
import com.trendizm.backend.ranking.application.dto.RankingSnapshot;
import com.trendizm.backend.ranking.application.dto.RankingSummaryResult;
import com.trendizm.backend.ranking.application.dto.RankingSummaryResult.SkippedKeyword;
import com.trendizm.backend.ranking.application.dto.RankingSummaryResult.SummarizedKeyword;
import com.trendizm.backend.ranking.application.port.RankingService;
import com.trendizm.backend.ranking.application.port.RankingSummaryService;
import com.trendizm.backend.ranking.application.port.out.RankingRepository;
import com.trendizm.backend.ranking.application.port.out.RankingSummaryGenerator;
import com.trendizm.backend.ranking.application.port.out.RankingSummaryGenerator.Answer;
import com.trendizm.backend.ranking.application.port.out.RankingSummaryGenerator.Post;
import com.trendizm.backend.ranking.application.port.out.RankingSummaryGenerator.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * AI 호출(수 초~1분)을 기다리는 동안 DB 연결을 잡고 있지 않게, 트랜잭션 밖(NOT_SUPPORTED)에서 부른다.
 * 1. 읽기: 최근 스냅샷은 RankingService의 짧은 트랜잭션으로 읽는다. 화면에 나오는 1~10위와 그 링크만 요약한다.
 * 2. AI 호출: 카테고리 하나를 요청 한 번으로 요약한다.
 * 3. 쓰기: 근거가 확인된 요약만 짧은 트랜잭션 하나로 저장한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingSummaryServiceImpl implements RankingSummaryService {

    private final RankingService rankingService;
    private final RankingRepository rankingRepository;
    private final RankingSummaryGenerator summaryGenerator;
    private final TransactionTemplate transactionTemplate;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public RankingSummaryResult summarizeLatest(String platform, String category, boolean overwrite) {
        RankingSnapshot snapshot = rankingService.getLatestSnapshot(platform, category);

        List<SkippedKeyword> skipped = new ArrayList<>();
        List<Target> targets = new ArrayList<>();
        for (RankingResponse ranking : snapshot.rankings()) {
            List<Post> posts = posts(ranking);
            if (ranking.summary() != null && !overwrite) {
                skipped.add(skip(ranking, "이미 요약이 있습니다."));
            } else if (posts.isEmpty()) {
                skipped.add(skip(ranking, "관련 게시물에 제목과 설명이 없습니다."));
            } else {
                targets.add(new Target(ranking.id(), ranking.keyword(), posts));
            }
        }

        List<SummarizedKeyword> summarized = new ArrayList<>();
        if (!targets.isEmpty()) {
            Map<Long, Answer> answers = summaryGenerator.summarize(targets).stream()
                    .collect(Collectors.toMap(Answer::rankingId, Function.identity()));
            Map<Long, RankingResponse> rankings = snapshot.rankings().stream()
                    .collect(Collectors.toMap(RankingResponse::id, Function.identity()));
            for (Target target : targets) {
                RankingResponse ranking = rankings.get(target.rankingId());
                Answer answer = answers.get(target.rankingId());
                Optional<String> reason = RankingSummaryRules.rejectReason(target, answer);
                if (reason.isPresent()) {
                    skipped.add(skip(ranking, reason.get()));
                } else {
                    summarized.add(new SummarizedKeyword(ranking.id(), ranking.rank(), ranking.keyword(),
                            RankingSummaryRules.oneLine(answer.summary())));
                }
            }
            transactionTemplate.executeWithoutResult(status -> summarized.forEach(keyword ->
                    rankingRepository.findById(keyword.rankingId())
                            .ifPresent(ranking -> ranking.changeSummary(keyword.summary()))));
        }
        return new RankingSummaryResult(snapshot.platform(), snapshot.categoryId(), snapshot.collectedAt(),
                summarized, skipped);
    }

    /** 제목이나 설명이 있는 링크만 글로 쓴다 */
    private static List<Post> posts(RankingResponse ranking) {
        return ranking.links().stream()
                .filter(RankingSummaryServiceImpl::hasText)
                .map(link -> new Post(link.title(), link.description()))
                .toList();
    }

    private static boolean hasText(RankingLinkResponse link) {
        return !RankingSummaryRules.oneLine(link.title()).isEmpty()
                || !RankingSummaryRules.oneLine(link.description()).isEmpty();
    }

    private static SkippedKeyword skip(RankingResponse ranking, String reason) {
        return new SkippedKeyword(ranking.id(), ranking.rank(), ranking.keyword(), reason);
    }
}
