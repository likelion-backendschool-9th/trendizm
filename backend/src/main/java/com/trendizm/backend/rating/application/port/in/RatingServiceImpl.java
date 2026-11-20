package com.trendizm.backend.rating.application.port.in;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.rating.application.dto.RatingContentQuery;
import com.trendizm.backend.rating.application.dto.RatingContentResponse;
import com.trendizm.backend.rating.application.dto.RatingContentSummaryResponse;
import com.trendizm.backend.rating.application.dto.RegisterRatingContentCommand;
import com.trendizm.backend.rating.application.dto.SentimentStats;
import com.trendizm.backend.rating.application.exception.RatingErrorCode;
import com.trendizm.backend.rating.application.port.RatingService;
import com.trendizm.backend.rating.application.port.out.RatingContentRepository;
import com.trendizm.backend.rating.application.port.out.RatingRepository;
import com.trendizm.backend.rating.application.port.out.SentimentCount;
import com.trendizm.backend.rating.domain.entity.RatingContent;
import com.trendizm.backend.rating.domain.enums.RatingContentExtraKey;
import com.trendizm.backend.rating.domain.enums.RatingContentStatus;
import com.trendizm.backend.rating.domain.enums.RatingContentSubcategory;
import com.trendizm.backend.rating.domain.enums.RatingContentVisibility;
import com.trendizm.backend.rating.domain.enums.Sentiment;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static final RatingContentVisibility PUBLIC = RatingContentVisibility.PUBLIC;

    private final RatingContentRepository contentRepository;
    private final RatingRepository ratingRepository;

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
        RatingContent content = contentRepository.findById(contentId)
                .filter(RatingContent::isPublic)
                .orElseThrow(() -> new BusinessException(RatingErrorCode.RATING_CONTENT_NOT_FOUND));
        return toResponse(content);
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
