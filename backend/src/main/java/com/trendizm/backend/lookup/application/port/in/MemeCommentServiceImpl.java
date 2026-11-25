package com.trendizm.backend.lookup.application.port.in;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.lookup.application.dto.CreateMemeCommentCommand;
import com.trendizm.backend.lookup.application.dto.MemeCommentResponse;
import com.trendizm.backend.lookup.application.dto.UpdateMemeCommentCommand;
import com.trendizm.backend.lookup.application.exception.MemeErrorCode;
import com.trendizm.backend.lookup.application.port.MemeCommentService;
import com.trendizm.backend.lookup.application.port.out.CountByKey;
import com.trendizm.backend.lookup.application.port.out.MemeCommentLikeRepository;
import com.trendizm.backend.lookup.application.port.out.MemeCommentRepository;
import com.trendizm.backend.lookup.application.port.out.MemeInterpretationRepository;
import com.trendizm.backend.lookup.application.port.out.MemeRepository;
import com.trendizm.backend.lookup.domain.entity.MemeComment;
import com.trendizm.backend.member.application.dto.MemberInfo;
import com.trendizm.backend.member.application.port.MemberService;
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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemeCommentServiceImpl implements MemeCommentService {

    /** 탈퇴했거나 없는 회원의 표시 이름 (item·rating과 같은 문구) */
    static final String UNKNOWN_AUTHOR = "탈퇴한 회원";

    private final MemeRepository memeRepository;
    private final MemeInterpretationRepository interpretationRepository;
    private final MemeCommentRepository commentRepository;
    private final MemeCommentLikeRepository commentLikeRepository;
    private final MemberService memberService;

    @Override
    public PageResponse<MemeCommentResponse> getComments(Long interpretationId, Long viewerId, int page, int size) {
        requireVisibleInterpretation(interpretationId);
        Page<MemeComment> comments = commentRepository.findVisibleComments(
                interpretationId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));

        List<MemeComment> replies = comments.isEmpty()
                ? List.of()
                : commentRepository.findByParentIdInAndDeletedAtIsNullOrderByIdAsc(comments.map(MemeComment::getId).getContent());

        List<MemeComment> all = new ArrayList<>(comments.getContent());
        all.addAll(replies);
        Function<MemeComment, MemeCommentResponse> toResponse = commentResponses(all, viewerId);

        Map<Long, List<MemeCommentResponse>> repliesByParent = replies.stream()
                .collect(Collectors.groupingBy(MemeComment::getParentId, LinkedHashMap::new,
                        Collectors.mapping(toResponse, Collectors.toList())));

        return PageResponse.from(comments, comment -> toResponse.apply(comment)
                .withReplies(repliesByParent.getOrDefault(comment.getId(), List.of())));
    }

    @Override
    @Transactional
    public MemeCommentResponse createComment(CreateMemeCommentCommand command) {
        requireVisibleInterpretation(command.interpretationId());
        if (command.parentId() != null) {
            // 부모를 잠가서, 부모 삭제와 답글 작성이 겹쳐 삭제된 댓글에 답글이 달리는 것을 막는다
            boolean validParent = commentRepository.findByIdForUpdate(command.parentId())
                    .filter(parent -> parent.getInterpretationId().equals(command.interpretationId()))
                    .filter(parent -> !parent.isReply())
                    .filter(parent -> !parent.isDeleted())
                    .isPresent();
            if (!validParent) {
                throw new BusinessException(MemeErrorCode.MEME_INVALID_PARENT_COMMENT);
            }
        }
        MemeComment comment = commentRepository.save(MemeComment.create(
                command.interpretationId(), command.parentId(), command.userId(), command.content()));
        return commentResponses(List.of(comment), command.userId()).apply(comment);
    }

    /** 내용만 바꾸고 변경 감지로 저장한다 */
    @Override
    @Transactional
    public MemeCommentResponse updateComment(UpdateMemeCommentCommand command) {
        MemeComment comment = getOwnComment(command.commentId(), command.userId());
        comment.edit(command.content());
        // updated_at은 flush할 때 채워지므로, 응답에 새 수정 시각이 나가도록 먼저 반영한다 (rating 후기 수정과 같은 방식)
        commentRepository.flush();
        return commentResponses(List.of(comment), command.userId()).apply(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        MemeComment comment = getOwnComment(commentId, userId);
        commentLikeRepository.deleteByCommentId(comment.getId());
        comment.markDeleted();
    }

    @Override
    @Transactional
    public void likeComment(Long commentId, Long userId) {
        getVisibleCommentForUpdate(commentId);
        commentLikeRepository.insertIfAbsent(commentId, userId);
    }

    @Override
    @Transactional
    public void unlikeComment(Long commentId, Long userId) {
        commentLikeRepository.deleteByCommentIdAndUserId(commentId, userId);
    }

    /** 의견이 없거나 그 의견의 문서가 숨김이면 MEME_INTERPRETATION_NOT_FOUND */
    private void requireVisibleInterpretation(Long interpretationId) {
        if (!isVisibleInterpretation(interpretationId)) {
            throw new BusinessException(MemeErrorCode.MEME_INTERPRETATION_NOT_FOUND);
        }
    }

    private boolean isVisibleInterpretation(Long interpretationId) {
        return interpretationRepository.findById(interpretationId)
                .flatMap(interpretation -> memeRepository.findById(interpretation.getMemeId()))
                .filter(meme -> !meme.isHidden())
                .isPresent();
    }

    /**
     * 댓글을 잠가서 읽는다. 없거나, 삭제됐거나, 문서가 숨김인 댓글이면 MEME_COMMENT_NOT_FOUND.
     * 수정·삭제·좋아요가 같은 댓글에서 겹치면 차례로 처리되어, 앞의 삭제가 끝난 뒤에는 삭제된 댓글로 보인다.
     */
    private MemeComment getVisibleCommentForUpdate(Long commentId) {
        return commentRepository.findByIdForUpdate(commentId)
                .filter(comment -> !comment.isDeleted())
                .filter(comment -> isVisibleInterpretation(comment.getInterpretationId()))
                .orElseThrow(() -> new BusinessException(MemeErrorCode.MEME_COMMENT_NOT_FOUND));
    }

    private MemeComment getOwnComment(Long commentId, Long userId) {
        MemeComment comment = getVisibleCommentForUpdate(commentId);
        if (!comment.isWrittenBy(userId)) {
            throw new BusinessException(MemeErrorCode.MEME_COMMENT_FORBIDDEN);
        }
        return comment;
    }

    /** 댓글들을 응답으로 바꾸는 함수. 작성자 닉네임·좋아요 수·내가 좋아요했는지를 한 번에 조회해 둔다 */
    private Function<MemeComment, MemeCommentResponse> commentResponses(Collection<MemeComment> comments, Long viewerId) {
        if (comments.isEmpty()) {
            return MemeCommentResponse::from;
        }
        List<Long> commentIds = comments.stream().map(MemeComment::getId).toList();
        Map<Long, String> authors = nicknames(comments.stream()
                .filter(comment -> !comment.isDeleted())
                .map(MemeComment::getUserId)
                .toList());
        Map<Long, Long> likeCounts = commentLikeRepository.countGroupByCommentId(commentIds).stream()
                .collect(Collectors.toMap(CountByKey::key, CountByKey::count));
        Set<Long> likedByViewer = viewerId == null
                ? Set.of()
                : new HashSet<>(commentLikeRepository.findLikedCommentIds(viewerId, commentIds));

        return comment -> MemeCommentResponse.from(comment)
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

    private static String displayName(MemberInfo member) {
        return member == null || member.withdrawn() ? UNKNOWN_AUTHOR : member.nickname();
    }
}
