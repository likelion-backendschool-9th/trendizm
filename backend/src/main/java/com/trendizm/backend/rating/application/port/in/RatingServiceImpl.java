package com.trendizm.backend.rating.application.port.in;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.rating.application.dto.RatingContentQuery;
import com.trendizm.backend.rating.application.dto.RatingContentResponse;
import com.trendizm.backend.rating.application.dto.RatingContentSummaryResponse;
import com.trendizm.backend.rating.application.dto.RatingReviewQuery;
import com.trendizm.backend.rating.application.dto.RatingReviewResponse;
import com.trendizm.backend.rating.application.dto.RatingReviewSaveResult;
import com.trendizm.backend.rating.application.dto.RegisterRatingContentCommand;
import com.trendizm.backend.rating.application.dto.SaveRatingReviewCommand;
import com.trendizm.backend.rating.application.dto.SentimentStats;
import com.trendizm.backend.rating.application.exception.RatingErrorCode;
import com.trendizm.backend.member.application.dto.MemberInfo;
import com.trendizm.backend.member.application.port.MemberService;
import com.trendizm.backend.point.application.port.PointService;
import com.trendizm.backend.rating.application.port.RatingService;
import com.trendizm.backend.rating.application.port.ReviewSort;
import com.trendizm.backend.rating.application.port.out.RatingContentRepository;
import com.trendizm.backend.rating.application.port.out.RatingRepository;
import com.trendizm.backend.rating.application.port.out.SentimentCount;
import com.trendizm.backend.rating.domain.entity.Rating;
import com.trendizm.backend.rating.domain.entity.RatingContent;
import com.trendizm.backend.rating.domain.enums.RatingContentExtraKey;
import com.trendizm.backend.rating.domain.enums.RatingContentStatus;
import com.trendizm.backend.rating.domain.enums.RatingContentSubcategory;
import com.trendizm.backend.rating.domain.enums.RatingContentVisibility;
import com.trendizm.backend.rating.domain.enums.Sentiment;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RatingServiceImpl implements RatingService {

    /** 후기를 처음 작성하면 주는 포인트 (팀 결정 11/20) */
    static final int REVIEW_REWARD_POINT = 10;

    /** 탈퇴했거나 찾을 수 없는 회원의 닉네임 자리에 보여 줄 이름 */
    static final String UNKNOWN_AUTHOR = "탈퇴한 회원";

    private static final RatingContentVisibility PUBLIC = RatingContentVisibility.PUBLIC;

    private final RatingContentRepository contentRepository;
    private final RatingRepository ratingRepository;
    private final MemberService memberService;
    private final PointService pointService;

    @Override
    public PageResponse<RatingContentSummaryResponse> getContents(RatingContentQuery query) {
        Pageable pageable = PageRequest.of(query.page(), query.size(), Sort.by(Sort.Direction.DESC, "id"));
        Page<RatingContent> contents = findPublicContents(query, pageable);
        Map<Long, SentimentStats> stats = statsByContentId(contents.map(RatingContent::getId).getContent());
        return PageResponse.from(contents, content -> RatingContentSummaryResponse.from(content)
                .withStats(stats.getOrDefault(content.getId(), SentimentStats.EMPTY)));
    }

    @Override
    public RatingContentResponse getContent(Long contentId) {
        return toResponse(getPublicContent(contentId));
    }

    @Override
    @Transactional
    public RatingContentResponse registerContent(RegisterRatingContentCommand command) {
        RatingContentSubcategory subcategory = RatingContentSubcategory.fromSlug(command.subcategory())
                .orElseThrow(() -> new BusinessException(RatingErrorCode.RATING_INVALID_CATEGORY));
        RatingContentStatus status = RatingContentStatus.fromSlug(command.status())
                .orElseThrow(() -> new BusinessException(RatingErrorCode.RATING_INVALID_STATUS));
        if (command.extra() != null
                && !command.extra().keySet().stream().allMatch(key -> RatingContentExtraKey.isAllowed(subcategory, key))) {
            throw new BusinessException(RatingErrorCode.RATING_INVALID_EXTRA_KEY);
        }
        RatingContent content = contentRepository.save(RatingContent.create(subcategory, status,
                command.title(), command.description(), command.imageUrl(), command.ageRating(),
                command.releasedAt(), command.extra()));
        return toResponse(content);
    }

    @Override
    @Transactional
    public void changeContentVisibility(Long contentId, boolean visible) {
        RatingContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new BusinessException(RatingErrorCode.RATING_CONTENT_NOT_FOUND));
        content.changeVisibility(visible ? RatingContentVisibility.PUBLIC : RatingContentVisibility.HIDDEN);
    }

    @Override
    public PageResponse<RatingReviewResponse> getReviews(RatingReviewQuery query) {
        getPublicContent(query.contentId());
        Pageable pageable = PageRequest.of(query.page(), query.size(), toSort(query.sort()));
        Page<Rating> reviews = query.sentiment() == null
                ? ratingRepository.findByContentId(query.contentId(), pageable)
                : ratingRepository.findByContentIdAndSentiment(query.contentId(), query.sentiment(), pageable);
        Map<Long, String> authors = nicknames(reviews.map(Rating::getUserId).getContent());
        return PageResponse.from(reviews, review -> RatingReviewResponse.from(review)
                .withAuthor(authors.get(review.getUserId()))
                .withMine(review.getUserId().equals(query.viewerId())));
    }

    @Override
    @Transactional
    public RatingReviewSaveResult saveReview(SaveRatingReviewCommand command) {
        getPublicContent(command.contentId());
        // 값 검사(별점 1~5 등)는 엔티티가 한다. 네이티브 INSERT 전에 같은 검사를 거치도록 먼저 만들어 본다
        Rating checked = Rating.create(command.contentId(), command.userId(), command.sentiment(), command.score(),
                command.title(), command.body(), command.progress());

        // 동시에 처음 작성해도 한 줄만 생기도록, 없을 때만 넣고 결과로 새로 만들었는지 판단한다
        boolean created = ratingRepository.insertIfAbsent(checked.getContentId(), checked.getUserId(),
                checked.getSentiment().name(), checked.getScore(), checked.getTitle(), checked.getBody(),
                checked.getProgress()) == 1;
        Rating review = ratingRepository.findByContentIdAndUserId(command.contentId(), command.userId())
                .orElseThrow(() -> new IllegalStateException("저장한 후기를 찾을 수 없습니다."));
        if (!created) {
            review.update(checked.getSentiment(), checked.getScore(), checked.getTitle(), checked.getBody(),
                    checked.getProgress());
        }

        int reward = 0;
        if (created) {
            // rewardForReview는 같은 후기에 한 번만 지급한다
            pointService.rewardForReview(command.userId(), review.getId(), REVIEW_REWARD_POINT);
            reward = REVIEW_REWARD_POINT;
        }
        RatingReviewResponse response = RatingReviewResponse.from(review)
                .withAuthor(nicknames(List.of(command.userId())).get(command.userId()))
                .withMine(true);
        return new RatingReviewSaveResult(response, created, reward);
    }

    private RatingContent getPublicContent(Long contentId) {
        return contentRepository.findById(contentId)
                .filter(RatingContent::isPublic)
                .orElseThrow(() -> new BusinessException(RatingErrorCode.RATING_CONTENT_NOT_FOUND));
    }

    private static Sort toSort(ReviewSort sort) {
        Sort latest = Sort.by(Sort.Direction.DESC, "id");
        return switch (sort == null ? ReviewSort.LATEST : sort) {
            case LATEST -> latest;
            case SCORE_HIGH -> Sort.by(Sort.Direction.DESC, "score").and(latest);
            case SCORE_LOW -> Sort.by(Sort.Direction.ASC, "score").and(latest);
        };
    }

    /** 회원 id → 닉네임. 탈퇴했거나 없는 회원은 UNKNOWN_AUTHOR */
    private Map<Long, String> nicknames(Collection<Long> userIds) {
        Set<Long> ids = new HashSet<>(userIds);
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, MemberInfo> members = memberService.getMembers(ids);
        return ids.stream().collect(Collectors.toMap(Function.identity(), id -> displayName(members.get(id))));
    }

    private static String displayName(MemberInfo member) {
        return member == null || member.withdrawn() ? UNKNOWN_AUTHOR : member.nickname();
    }

    private Page<RatingContent> findPublicContents(RatingContentQuery query, Pageable pageable) {
        if (query.subcategory() != null) {
            if (query.category() != null && query.subcategory().getCategory() != query.category()) {
                throw new BusinessException(RatingErrorCode.RATING_CATEGORY_MISMATCH);
            }
            return query.status() == null
                    ? contentRepository.findByVisibilityAndSubcategory(PUBLIC, query.subcategory(), pageable)
                    : contentRepository.findByVisibilityAndSubcategoryAndStatus(
                            PUBLIC, query.subcategory(), query.status(), pageable);
        }
        if (query.category() != null) {
            return query.status() == null
                    ? contentRepository.findByVisibilityAndCategory(PUBLIC, query.category(), pageable)
                    : contentRepository.findByVisibilityAndCategoryAndStatus(
                            PUBLIC, query.category(), query.status(), pageable);
        }
        return query.status() == null
                ? contentRepository.findByVisibility(PUBLIC, pageable)
                : contentRepository.findByVisibilityAndStatus(PUBLIC, query.status(), pageable);
    }

    private RatingContentResponse toResponse(RatingContent content) {
        return RatingContentResponse.from(content)
                .withStats(statsByContentId(List.of(content.getId())).getOrDefault(content.getId(), SentimentStats.EMPTY));
    }

    /** 콘텐츠 id → 호/불호 집계. 후기가 없는 콘텐츠는 결과에 없다 */
    private Map<Long, SentimentStats> statsByContentId(Collection<Long> contentIds) {
        if (contentIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, SentimentCount> likes = new HashMap<>();
        Map<Long, SentimentCount> dislikes = new HashMap<>();
        for (SentimentCount count : ratingRepository.countGroupByContentIdAndSentiment(contentIds)) {
            (count.sentiment() == Sentiment.LIKE ? likes : dislikes).put(count.contentId(), count);
        }
        Map<Long, SentimentStats> stats = new HashMap<>();
        for (Long contentId : contentIds) {
            SentimentCount like = likes.get(contentId);
            SentimentCount dislike = dislikes.get(contentId);
            if (like == null && dislike == null) {
                continue;
            }
            stats.put(contentId, SentimentStats.of(
                    like == null ? 0 : like.count(),
                    dislike == null ? 0 : dislike.count(),
                    like == null ? null : like.averageScore(),
                    dislike == null ? null : dislike.averageScore()));
        }
        return stats;
    }
}
