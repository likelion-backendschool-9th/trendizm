"use client";

import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";
import { formatDate } from "@/app/item/_lib/format";
import type { ItemComment } from "@/app/item/_lib/types";
import { apiFetch } from "@/lib/api/client";
import { ApiError, type PageResponse } from "@/lib/api/types";

type CommentsProps = {
  questionId: number;
  commentCount: number;
  /** 서버에서 받은 첫 페이지(최신 댓글) */
  initialPage: PageResponse<ItemComment>;
  pageSize: number;
};

// 댓글 목록(최신순, 대댓글은 오래된 순)과 작성·답글·공감.
// 첫 페이지는 서버에서 받고, "더 보기"와 작성 후 새로 고침은 apiFetch로 직접 부른다.
// 쓰기 요청에서 로그인하지 않았으면 apiFetch가 로그인 화면으로 보낸다.
export default function Comments({
  questionId,
  commentCount,
  initialPage,
  pageSize,
}: CommentsProps) {
  const router = useRouter();
  const [comments, setComments] = useState(initialPage.content);
  const [page, setPage] = useState(initialPage.page);
  const [hasNext, setHasNext] = useState(initialPage.hasNext);
  const [loadingMore, setLoadingMore] = useState(false);

  async function loadMore() {
    setLoadingMore(true);
    try {
      const next = await apiFetch<PageResponse<ItemComment>>(
        `/api/items/${questionId}/comments?page=${page + 1}&size=${pageSize}`,
        { redirectOnUnauthorized: false },
      );
      setComments((current) => [...current, ...next.content]);
      setPage(next.page);
      setHasNext(next.hasNext);
    } finally {
      setLoadingMore(false);
    }
  }

  /** 새 댓글이 맨 위에 보이도록 첫 페이지부터 다시 받는다. 위쪽 댓글 수도 맞추려고 서버 화면도 새로 고친다 */
  async function reload() {
    const first = await apiFetch<PageResponse<ItemComment>>(
      `/api/items/${questionId}/comments?page=0&size=${pageSize}`,
      { redirectOnUnauthorized: false },
    );
    setComments(first.content);
    setPage(first.page);
    setHasNext(first.hasNext);
    router.refresh();
  }

  /** 공감·취소. 화면을 먼저 바꾸고, 실패하면 되돌린다 */
  async function toggleLike(target: ItemComment) {
    const update = (liked: boolean) =>
      setComments((current) =>
        current.map((comment) => applyLike(comment, target.id, liked)),
      );
    update(!target.likedByMe);
    try {
      await apiFetch(`/api/items/comments/${target.id}/likes`, {
        method: target.likedByMe ? "DELETE" : "POST",
      });
    } catch {
      update(target.likedByMe);
    }
  }

  return (
    <section id="comments" style={{ marginTop: 12 }}>
      <h2 style={{ marginBottom: 20, fontSize: 20, fontWeight: 700 }}>
        댓글 {commentCount}
      </h2>

      <div style={{ border: "1px solid #111" }}>
        <CommentForm
          questionId={questionId}
          placeholder="궁금한 점이나 의견을 남겨 주세요"
          onDone={reload}
        />

        {comments.length === 0 ? (
          <p
            style={{
              padding: "32px 0",
              textAlign: "center",
              fontSize: 14,
              color: "#777",
            }}
          >
            첫 댓글을 남겨 보세요.
          </p>
        ) : (
          <ul style={{ listStyle: "none" }}>
            {comments.map((comment, index) => (
              <CommentItem
                key={comment.id}
                questionId={questionId}
                comment={comment}
                first={index === 0}
                onLike={toggleLike}
                onReplied={reload}
              />
            ))}
          </ul>
        )}
      </div>

      {hasNext && (
        <button
          type="button"
          onClick={loadMore}
          disabled={loadingMore}
          style={{
            display: "block",
            width: "100%",
            marginTop: 12,
            padding: "12px 0",
            border: "1px solid #111",
            background: "#fff",
            fontSize: 14,
            cursor: loadingMore ? "wait" : "pointer",
          }}
        >
          {loadingMore ? "불러오는 중..." : "댓글 더 보기"}
        </button>
      )}
    </section>
  );
}

function applyLike(comment: ItemComment, targetId: number, liked: boolean): ItemComment {
  if (comment.id === targetId) {
    return {
      ...comment,
      likedByMe: liked,
      likeCount: comment.likeCount + (liked ? 1 : -1),
    };
  }
  return {
    ...comment,
    replies: comment.replies.map((reply) => applyLike(reply, targetId, liked)),
  };
}

function CommentItem({
  questionId,
  comment,
  first,
  onLike,
  onReplied,
}: {
  questionId: number;
  comment: ItemComment;
  first: boolean;
  onLike: (comment: ItemComment) => void;
  onReplied: () => Promise<void>;
}) {
  const [replying, setReplying] = useState(false);

  return (
    <li
      style={{
        display: "flex",
        gap: 14,
        padding: "22px 24px",
        borderTop: first ? "none" : "1px solid #ddd",
      }}
    >
      <span
        aria-hidden
        style={{ flexShrink: 0, width: 40, height: 40, background: "#e5e5e5" }}
      />
      <div style={{ flex: 1, minWidth: 0 }}>
        <CommentBody comment={comment} />
        <div
          style={{
            display: "flex",
            gap: 24,
            marginTop: 12,
            fontSize: 12,
            color: "#777",
          }}
        >
          <LikeButton comment={comment} onLike={onLike} />
          <button
            type="button"
            onClick={() => setReplying((open) => !open)}
            aria-expanded={replying}
            style={{
              border: "none",
              background: "none",
              color: "inherit",
              cursor: "pointer",
            }}
          >
            {replying ? "답글 취소" : "답글 달기"}
          </button>
        </div>

        {replying && (
          <div style={{ marginTop: 12, border: "1px solid #ddd" }}>
            <CommentForm
              questionId={questionId}
              parentId={comment.id}
              placeholder="답글을 입력해 주세요"
              onDone={async () => {
                setReplying(false);
                await onReplied();
              }}
            />
          </div>
        )}

        {comment.replies.length > 0 && (
          <ul
            style={{
              marginTop: 18,
              listStyle: "none",
              border: "1px solid #ddd",
              background: "#f7f7f7",
            }}
          >
            {comment.replies.map((reply) => (
              <li key={reply.id} style={{ display: "flex", gap: 12, padding: 16 }}>
                <span style={{ color: "#777" }}>↳</span>
                <span
                  aria-hidden
                  style={{
                    flexShrink: 0,
                    width: 32,
                    height: 32,
                    background: "#e5e5e5",
                  }}
                />
                <div style={{ minWidth: 0 }}>
                  <CommentBody comment={reply} />
                  <div style={{ marginTop: 10, fontSize: 12, color: "#777" }}>
                    <LikeButton comment={reply} onLike={onLike} />
                  </div>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </li>
  );
}

function CommentBody({ comment }: { comment: ItemComment }) {
  return (
    <>
      <p style={{ display: "flex", alignItems: "center", gap: 8, fontSize: 13 }}>
        <span style={{ fontWeight: 700 }}>{comment.authorNickname}</span>
        <span style={{ color: "#777" }}>{formatDate(comment.createdAt)}</span>
      </p>
      <p style={{ marginTop: 8, fontSize: 14, whiteSpace: "pre-wrap" }}>
        {comment.content}
      </p>
    </>
  );
}

function LikeButton({
  comment,
  onLike,
}: {
  comment: ItemComment;
  onLike: (comment: ItemComment) => void;
}) {
  return (
    <button
      type="button"
      onClick={() => onLike(comment)}
      aria-pressed={comment.likedByMe}
      style={{
        border: "none",
        background: "none",
        color: comment.likedByMe ? "#111" : "inherit",
        fontWeight: comment.likedByMe ? 700 : 400,
        cursor: "pointer",
      }}
    >
      {comment.likedByMe ? "♥" : "♡"} 공감 {comment.likeCount}
    </button>
  );
}

function CommentForm({
  questionId,
  parentId,
  placeholder,
  onDone,
}: {
  questionId: number;
  parentId?: number;
  placeholder: string;
  onDone: () => Promise<void>;
}) {
  const [content, setContent] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!content.trim()) {
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      await apiFetch(`/api/items/${questionId}/comments`, {
        method: "POST",
        body: { parentId: parentId ?? null, content: content.trim() },
      });
      setContent("");
      await onDone();
    } catch (caught) {
      if (caught instanceof ApiError && caught.status !== 401) {
        setError(caught.fieldMessage("content") ?? caught.message);
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={submit} style={{ padding: 16, borderBottom: "1px solid #ddd" }}>
      <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
        <input
          type="text"
          value={content}
          onChange={(event) => setContent(event.target.value)}
          placeholder={placeholder}
          maxLength={500}
          aria-label={parentId ? "답글 내용" : "댓글 내용"}
          style={{
            flex: 1,
            minWidth: 0,
            height: 44,
            padding: "0 14px",
            border: "1px solid #111",
            fontSize: 14,
          }}
        />
        <button
          type="submit"
          disabled={submitting || !content.trim()}
          style={{
            height: 44,
            padding: "0 20px",
            border: "none",
            background: "#111",
            color: "#fff",
            fontSize: 14,
            fontWeight: 700,
            cursor: submitting ? "wait" : "pointer",
            opacity: submitting || !content.trim() ? 0.5 : 1,
          }}
        >
          등록
        </button>
      </div>
      {error && (
        <p role="alert" style={{ marginTop: 8, fontSize: 12, color: "#c00" }}>
          {error}
        </p>
      )}
    </form>
  );
}
