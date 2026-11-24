package com.trendizm.backend.item.application.port.in;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.item.application.dto.CreateItemCommand;
import com.trendizm.backend.item.application.dto.ItemAdjacentQuestions;
import com.trendizm.backend.item.application.dto.ItemQuestionLinkResponse;
import com.trendizm.backend.item.application.dto.CreateItemCommentCommand;
import com.trendizm.backend.item.application.dto.CreateItemQuestionCommand;
import com.trendizm.backend.item.application.dto.ItemCommentResponse;
import com.trendizm.backend.item.application.dto.ItemQuestionQuery;
import com.trendizm.backend.item.application.dto.ItemQuestionResponse;
import com.trendizm.backend.item.application.dto.ItemQuestionSummaryResponse;
import com.trendizm.backend.item.application.dto.ItemResponse;
import com.trendizm.backend.item.application.exception.ItemErrorCode;
import com.trendizm.backend.item.application.port.ItemCommentSort;
import com.trendizm.backend.item.application.port.ItemService;
import com.trendizm.backend.item.application.port.out.CountByKey;
import com.trendizm.backend.item.application.port.out.ItemCommentLikeRepository;
import com.trendizm.backend.item.application.port.out.ItemCommentRepository;
import com.trendizm.backend.item.application.port.out.ItemQuestionRepository;
import com.trendizm.backend.item.application.port.out.ItemRepository;
import com.trendizm.backend.item.application.port.out.ItemUnlockRepository;
import com.trendizm.backend.item.domain.entity.Item;
import com.trendizm.backend.item.domain.entity.ItemComment;
import com.trendizm.backend.item.domain.entity.ItemQuestion;
import com.trendizm.backend.item.domain.entity.ItemUnlock;
import com.trendizm.backend.item.domain.enums.ItemQuestionStatus;
import com.trendizm.backend.member.application.dto.MemberInfo;
import com.trendizm.backend.member.application.port.MemberService;
import com.trendizm.backend.point.application.dto.UnlockPaymentCommand;
import com.trendizm.backend.point.application.port.PointService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
public class ItemServiceImpl implements ItemService {

    /** 열람 가격 중 등록자에게 적립하는 비율(%). 지금은 전액 적립 (ERD item_unlocks 주석) */
    static final int EARN_RATE_PERCENT = 100;

    /** 탈퇴했거나 찾을 수 없는 회원의 닉네임 자리에 보여 줄 이름 */
    static final String UNKNOWN_AUTHOR = "탈퇴한 회원";

    private final ItemQuestionRepository questionRepository;
    private final ItemCommentRepository commentRepository;
    private final ItemCommentLikeRepository commentLikeRepository;
    private final ItemRepository itemRepository;
    private final ItemUnlockRepository unlockRepository;
    private final MemberService memberService;
    private final PointService pointService;

    @Override
    public PageResponse<ItemQuestionSummaryResponse> getQuestions(ItemQuestionQuery query) {
        Pageable pageable = PageRequest.of(query.page(), query.size(), Sort.by(Sort.Direction.DESC, "id"));
        Page<ItemQuestion> questions = findPublicQuestions(query, pageable);

        List<Long> questionIds = questions.map(ItemQuestion::getId).getContent();
        Map<Long, Long> commentCounts = questionIds.isEmpty()
                ? Map.of() : toMap(commentRepository.countGroupByQuestionId(questionIds));
        Map<Long, Long> itemCounts = questionIds.isEmpty()
                ? Map.of() : toMap(itemRepository.countGroupByQuestionId(questionIds));
        Map<Long, String> authors = nicknames(questions.map(ItemQuestion::getUserId).getContent());

        return PageResponse.from(questions, question -> ItemQuestionSummaryResponse.from(question)
                .withAuthor(authors.get(question.getUserId()))
                .withCounts(commentCounts.getOrDefault(question.getId(), 0L),
                        itemCounts.getOrDefault(question.getId(), 0L)));
    }

    @Override
    @Transactional
    public ItemQuestionResponse getQuestion(Long questionId, Long viewerId) {
        ItemQuestion question = getPublicQuestion(questionId);
        question.increaseViewCount();
        return toResponse(question, viewerId);
    }

    @Override
    public ItemAdjacentQuestions getAdjacentQuestions(Long questionId) {
        getPublicQuestion(questionId);
        ItemQuestionLinkResponse previous = questionRepository
                .findFirstByStatusAndIdLessThanOrderByIdDesc(ItemQuestionStatus.PUBLIC, questionId)
                .map(ItemQuestionLinkResponse::from).orElse(null);
        ItemQuestionLinkResponse next = questionRepository
                .findFirstByStatusAndIdGreaterThanOrderByIdAsc(ItemQuestionStatus.PUBLIC, questionId)
                .map(ItemQuestionLinkResponse::from).orElse(null);
        return new ItemAdjacentQuestions(previous, next);
    }

    @Override
    @Transactional
    public ItemQuestionResponse createQuestion(CreateItemQuestionCommand command) {
        ItemQuestion question = questionRepository.save(ItemQuestion.create(
                command.userId(), command.subcategory(), command.title(), command.content()));
        return toResponse(question, command.userId());
    }

    @Override
    @Transactional
    public void changeQuestionVisibility(Long questionId, boolean visible) {
        ItemQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(ItemErrorCode.ITEM_QUESTION_NOT_FOUND));
        question.changeStatus(visible ? ItemQuestionStatus.PUBLIC : ItemQuestionStatus.HIDDEN);
    }

    @Override
    public PageResponse<ItemCommentResponse> getComments(Long questionId, Long viewerId, ItemCommentSort sort,
                                                         int page, int size) {
        getPublicQuestion(questionId);
        Page<ItemComment> comments = sort == ItemCommentSort.LIKES
                ? commentRepository.findByQuestionIdAndParentIdIsNullOrderByLikes(questionId, PageRequest.of(page, size))
                : commentRepository.findByQuestionIdAndParentIdIsNull(
                        questionId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));

        List<ItemComment> replies = comments.isEmpty()
                ? List.of()
                : commentRepository.findByParentIdInOrderByIdAsc(comments.map(ItemComment::getId).getContent());

        List<ItemComment> all = new ArrayList<>(comments.getContent());
        all.addAll(replies);
        Function<ItemComment, ItemCommentResponse> toResponse = commentResponses(all, viewerId);

        Map<Long, List<ItemCommentResponse>> repliesByParent = replies.stream()
                .collect(Collectors.groupingBy(ItemComment::getParentId, LinkedHashMap::new,
                        Collectors.mapping(toResponse, Collectors.toList())));

        return PageResponse.from(comments, comment -> toResponse.apply(comment)
                .withReplies(repliesByParent.getOrDefault(comment.getId(), List.of())));
    }

    @Override
    @Transactional
    public ItemCommentResponse createComment(CreateItemCommentCommand command) {
        getPublicQuestion(command.questionId());
        if (command.parentId() != null) {
            boolean validParent = commentRepository.findById(command.parentId())
                    .filter(parent -> parent.getQuestionId().equals(command.questionId()))
                    .filter(parent -> !parent.isReply())
                    .isPresent();
            if (!validParent) {
                throw new BusinessException(ItemErrorCode.ITEM_INVALID_PARENT_COMMENT);
            }
        }
        ItemComment comment = commentRepository.save(ItemComment.create(
                command.questionId(), command.parentId(), command.userId(), command.content()));
        return commentResponses(List.of(comment), command.userId()).apply(comment);
    }

    @Override
    @Transactional
    public void likeComment(Long commentId, Long userId) {
        ItemComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ItemErrorCode.ITEM_COMMENT_NOT_FOUND));
        if (!isPublicQuestion(comment.getQuestionId())) {
            throw new BusinessException(ItemErrorCode.ITEM_COMMENT_NOT_FOUND);
        }
        commentLikeRepository.insertIfAbsent(commentId, userId);
    }

    @Override
    @Transactional
    public void unlikeComment(Long commentId, Long userId) {
        commentLikeRepository.deleteByCommentIdAndUserId(commentId, userId);
    }

    @Override
    public List<ItemResponse> getRecommendations(Long questionId, Long viewerId) {
        getPublicQuestion(questionId);
        List<Item> items = itemRepository.findByQuestionIdOrderByIdAsc(questionId);
        Map<Long, String> authors = nicknames(items.stream().map(Item::getUserId).toList());
        Set<Long> unlocked = viewerId == null || items.isEmpty()
                ? Set.of()
                : new HashSet<>(unlockRepository.findUnlockedItemIds(viewerId, items.stream().map(Item::getId).toList()));
        return items.stream()
                .map(item -> toResponse(item, authors.get(item.getUserId()), viewerId, unlocked.contains(item.getId())))
                .toList();
    }

    @Override
    @Transactional
    public ItemResponse unlockRecommendation(Long recommendationId, Long userId) {
        Item item = itemRepository.findById(recommendationId)
                .filter(found -> isPublicQuestion(found.getQuestionId()))
                .orElseThrow(() -> new BusinessException(ItemErrorCode.ITEM_RECOMMENDATION_NOT_FOUND));
        String author = nickname(item.getUserId());

        // 내가 등록한 아이템은 포인트 없이 처음부터 열려 있다 (팀 결정 11/18)
        if (item.isOwnedBy(userId)) {
            return toResponse(item, author, userId, false);
        }

        // 열람 기록을 없을 때만 넣는다. 0이면 이미 열람했거나, 같은 회원의 다른 요청이 먼저 결제를 끝낸 경우다
        int price = item.getUnlockPrice();
        boolean firstUnlock = unlockRepository.insertIfAbsent(item.getId(), userId, price) == 1;
        if (firstUnlock) {
            ItemUnlock unlock = unlockRepository.findByItemIdAndUserId(item.getId(), userId)
                    .orElseThrow(() -> new IllegalStateException("저장한 열람 기록을 찾을 수 없습니다."));
            // 포인트가 부족하면 예외가 나서 이 트랜잭션 전체(열람 기록 포함)가 롤백된다
            pointService.payForUnlock(new UnlockPaymentCommand(userId, item.getUserId(), unlock.getId(), price,
                    price * EARN_RATE_PERCENT / 100));
        }
        return toResponse(item, author, userId, true);
    }

    @Override
    @Transactional
    public ItemResponse createRecommendation(CreateItemCommand command) {
        getPublicQuestion(command.questionId());
        // TODO(item): 회원 사용자화 정보(3.5)가 생기면 MemberService.findProfiles(memberIds)로
        //  등록자 연령대·성별을 받아 targetAgeGroup, targetGender에 복사한다 (입력하지 않은 회원은 null)
        Item item = itemRepository.save(Item.create(command.questionId(), command.userId(), command.title(),
                command.seller(), command.price(), command.tags(), command.description(),
                command.imageUrl(), command.linkUrl()));
        return toResponse(item, nickname(command.userId()), command.userId(), false);
    }

    private Page<ItemQuestion> findPublicQuestions(ItemQuestionQuery query, Pageable pageable) {
        if (query.subcategory() != null) {
            if (query.category() != null && query.subcategory().getCategory() != query.category()) {
                throw new BusinessException(ItemErrorCode.ITEM_CATEGORY_MISMATCH);
            }
            return questionRepository.findByStatusAndSubcategory(ItemQuestionStatus.PUBLIC, query.subcategory(), pageable);
        }
        if (query.category() != null) {
            return questionRepository.findByStatusAndCategory(ItemQuestionStatus.PUBLIC, query.category(), pageable);
        }
        return questionRepository.findByStatus(ItemQuestionStatus.PUBLIC, pageable);
    }

    private ItemQuestion getPublicQuestion(Long questionId) {
        return questionRepository.findById(questionId)
                .filter(ItemQuestion::isPublic)
                .orElseThrow(() -> new BusinessException(ItemErrorCode.ITEM_QUESTION_NOT_FOUND));
    }

    private boolean isPublicQuestion(Long questionId) {
        return questionRepository.findById(questionId).map(ItemQuestion::isPublic).orElse(false);
    }

    private ItemQuestionResponse toResponse(ItemQuestion question, Long viewerId) {
        return ItemQuestionResponse.from(question)
                .withAuthor(nickname(question.getUserId()))
                .withMine(question.getUserId().equals(viewerId))
                .withCounts(commentRepository.countByQuestionId(question.getId()),
                        itemRepository.countByQuestionId(question.getId()));
    }

    /** 등록자 본인과 열람한 회원에게만 열린 상태로 보여 준다 */
    private ItemResponse toResponse(Item item, String authorNickname, Long viewerId, boolean unlocked) {
        boolean mine = item.isOwnedBy(viewerId);
        ItemResponse response = ItemResponse.from(item).withAuthor(authorNickname).withMine(mine);
        return mine || unlocked ? response : response.lock();
    }

    /** 댓글들을 응답으로 바꾸는 함수. 작성자 닉네임·공감 수·내가 공감했는지를 한 번에 조회해 둔다 */
    private Function<ItemComment, ItemCommentResponse> commentResponses(Collection<ItemComment> comments, Long viewerId) {
        if (comments.isEmpty()) {
            return ItemCommentResponse::from;
        }
        List<Long> commentIds = comments.stream().map(ItemComment::getId).toList();
        Map<Long, String> authors = nicknames(comments.stream().map(ItemComment::getUserId).toList());
        Map<Long, Long> likeCounts = toMap(commentLikeRepository.countGroupByCommentId(commentIds));
        Set<Long> likedByViewer = viewerId == null
                ? Set.of()
                : new HashSet<>(commentLikeRepository.findLikedCommentIds(viewerId, commentIds));

        return comment -> ItemCommentResponse.from(comment)
                .withAuthor(authors.get(comment.getUserId()))
                .withLikes(likeCounts.getOrDefault(comment.getId(), 0L), likedByViewer.contains(comment.getId()));
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

    private String nickname(Long userId) {
        return nicknames(List.of(userId)).get(userId);
    }

    private static String displayName(MemberInfo member) {
        return member == null || member.withdrawn() ? UNKNOWN_AUTHOR : member.nickname();
    }

    private static Map<Long, Long> toMap(List<CountByKey> counts) {
        return counts.stream().collect(Collectors.toMap(CountByKey::key, CountByKey::count));
    }
}
