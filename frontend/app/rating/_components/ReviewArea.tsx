"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";
import Stars from "@/app/rating/_components/Stars";
import { formatDate } from "@/app/rating/_lib/format";
import {
  REVIEW_SORTS,
  type RatingReviewItem,
  type RatingReviewSaveResult,
  type ReviewSort,
  type Sentiment,
} from "@/app/rating/_lib/types";
import { apiFetch } from "@/lib/api/client";
import { ApiError, type PageResponse } from "@/lib/api/types";

type ReviewAreaProps = {
  contentId: number;
  sentiment: Sentiment;
  /** 서버에서 받은 첫 페이지(최신순) */
  initialPage: PageResponse<RatingReviewItem>;
  /** "몇 화까지 봤는지"를 물어볼지 (회차가 있는 소분류) */
  askProgress: boolean;
};

const PAGE_SIZE = 10;
const BODY_MAX = 2000;
const SENTIMENT_NAME: Record<Sentiment, string> = { like: "좋아요", dislike: "싫어요" };

// 고른 쪽(좋아요·싫어요) 후기 목록과 후기 작성·수정 폼.
// 정렬·더 보기·공감은 apiFetch로 직접 부르고, 저장하면 목록을 다시 받고 위쪽 집계는 router.refresh()로 새로 그린다.
// 저장 API는 이미 쓴 후기가 있으면 수정한다. 내 후기가 불러온 목록에 있으면 그 내용으로 폼을 채운다.
// TODO(rating): "내 후기 조회" API가 생기면(개인화) 목록에 없을 때도 폼을 채운다.
export default function ReviewArea({ contentId, sentiment, initialPage, askProgress }: ReviewAreaProps) {
  const router = useRouter();
  const [sort, setSort] = useState<ReviewSort>("latest");
  const [reviews, setReviews] = useState(initialPage.content);
  const [page, setPage] = useState(initialPage.page);
  const [hasNext, setHasNext] = useState(initialPage.hasNext);
  const [totalElements, setTotalElements] = useState(initialPage.totalElements);
  const [loading, setLoading] = useState(false);

  async function fetchPage(nextSort: ReviewSort, nextPage: number) {
    return apiFetch<PageResponse<RatingReviewItem>>(
      `/api/ratings/${contentId}/reviews?sentiment=${sentiment}&sort=${nextSort}&page=${nextPage}&size=${PAGE_SIZE}`,
      { redirectOnUnauthorized: false },
    );
  }

  async function changeSort(nextSort: ReviewSort) {
    setLoading(true);
    try {
      const first = await fetchPage(nextSort, 0);
      setSort(nextSort);
      setReviews(first.content);
      setPage(first.page);
      setHasNext(first.hasNext);
      setTotalElements(first.totalElements);
    } finally {
      setLoading(false);
    }
  }

  async function loadMore() {
    setLoading(true);
    try {
      const next = await fetchPage(sort, page + 1);
      setReviews((current) => [...current, ...next.content]);
      setPage(next.page);
      setHasNext(next.hasNext);
    } finally {
      setLoading(false);
    }
  }

  /** 공감·취소. 화면을 먼저 바꾸고, 실패하면 되돌린다 */
  async function toggleLike(target: RatingReviewItem) {
    const update = (liked: boolean) =>
      setReviews((current) =>
        current.map((review) =>
          review.id === target.id
            ? { ...review, likedByMe: liked, likeCount: review.likeCount + (liked ? 1 : -1) }
            : review,
        ),
      );
    update(!target.likedByMe);
    try {
      await apiFetch(`/api/ratings/reviews/${target.id}/likes`, {
        method: target.likedByMe ? "DELETE" : "POST",
      });
    } catch {
      update(target.likedByMe);
    }
  }

  // 저장 후 안내. 폼은 저장하면 새 내용으로 다시 그려지므로 안내는 여기 둔다
  const [notice, setNotice] = useState<{ text: string; moved?: Sentiment } | null>(null);

  async function afterSave(text: string, moved?: Sentiment) {
    setNotice({ text, moved });
    await changeSort(sort);
    router.refresh();
  }

  const myReview = reviews.find((review) => review.mine);

  return (
    <>
      <section style={{ marginTop: 28 }}>
        <header
          style={{
            display: "flex",
            flexWrap: "wrap",
            justifyContent: "space-between",
            alignItems: "center",
            gap: 12,
            marginBottom: 16,
          }}
        >
          <h2 style={{ fontSize: 20, fontWeight: 700 }}>
            {SENTIMENT_NAME[sentiment]} 후기 {totalElements}
          </h2>
          <div role="group" aria-label="정렬" style={{ display: "flex", gap: 6 }}>
            {REVIEW_SORTS.map((option) => (
              <button
                key={option.slug}
                type="button"
                onClick={() => changeSort(option.slug)}
                aria-pressed={sort === option.slug}
                disabled={loading}
                style={{
                  padding: "6px 10px",
                  border: "1px solid #222",
                  borderRadius: 6,
                  background: sort === option.slug ? "#111" : "#fff",
                  color: sort === option.slug ? "#fff" : "#111",
                  fontSize: 12,
                  cursor: loading ? "wait" : "pointer",
                }}
              >
                {option.name}
              </button>
            ))}
          </div>
        </header>

        {reviews.length === 0 ? (
          <p
            style={{
              padding: "40px 0",
              border: "1px solid #ddd",
              borderRadius: 12,
              textAlign: "center",
              fontSize: 14,
              color: "#777",
            }}
          >
            아직 {SENTIMENT_NAME[sentiment]} 후기가 없어요. 첫 후기를 남겨 보세요.
          </p>
        ) : (
          <ul style={{ display: "flex", flexDirection: "column", gap: 12, listStyle: "none" }}>
            {reviews.map((review) => (
              <li
                key={review.id}
                style={{
                  padding: "16px 18px",
                  border: review.mine ? "2px solid #111" : "1px solid #222",
                  borderRadius: 8,
                }}
              >
                <p style={{ display: "flex", alignItems: "center", gap: 8, fontSize: 13 }}>
                  <strong>{review.authorNickname}</strong>
                  {review.mine && (
                    <span
                      style={{
                        padding: "1px 6px",
                        border: "1px solid #111",
                        borderRadius: 3,
                        fontSize: 11,
                      }}
                    >
                      내 후기
                    </span>
                  )}
                  <span style={{ marginLeft: "auto", fontSize: 11, color: "#777" }}>
                    {formatDate(review.createdAt)}
                    {review.updatedAt !== review.createdAt && " (수정됨)"}
                  </span>
                </p>
                <p style={{ display: "flex", alignItems: "center", gap: 10, marginTop: 10 }}>
                  <Stars score={review.score} size={12} />
                  <span style={{ fontSize: 12 }}>{review.score.toFixed(1)}</span>
                  <strong style={{ fontSize: 15 }}>{review.title}</strong>
                </p>
                <p style={{ marginTop: 10, fontSize: 14, lineHeight: 1.6, whiteSpace: "pre-wrap" }}>
                  {review.body}
                </p>
                <p
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    marginTop: 12,
                    fontSize: 12,
                  }}
                >
                  <button
                    type="button"
                    onClick={() => toggleLike(review)}
                    aria-pressed={review.likedByMe}
                    style={{
                      padding: "4px 10px",
                      border: "1px solid #222",
                      borderRadius: 4,
                      background: review.likedByMe ? "#111" : "#fff",
                      color: review.likedByMe ? "#fff" : "#111",
                      fontSize: 12,
                      cursor: "pointer",
                    }}
                  >
                    {review.likedByMe ? "♥" : "♡"} 공감 {review.likeCount}
                  </button>
                  {review.progress !== null && (
                    <span
                      style={{
                        padding: "1px 8px",
                        border: "1px solid #222",
                        borderRadius: 3,
                        fontSize: 11,
                      }}
                    >
                      {review.progress}화까지 봄
                    </span>
                  )}
                </p>
              </li>
            ))}
          </ul>
        )}

        {hasNext && (
          <button
            type="button"
            onClick={loadMore}
            disabled={loading}
            style={{
              display: "block",
              width: "100%",
              marginTop: 12,
              padding: "12px 0",
              border: "1px solid #222",
              borderRadius: 8,
              background: "#fff",
              fontSize: 14,
              cursor: loading ? "wait" : "pointer",
            }}
          >
            {loading ? "불러오는 중..." : "후기 더 보기"}
          </button>
        )}
      </section>

      {notice && (
        <p
          role="status"
          style={{
            marginTop: 32,
            padding: "10px 14px",
            border: "1px solid #111",
            borderRadius: 6,
            fontSize: 13,
          }}
        >
          {notice.text}
          {notice.moved && (
            <>
              {" "}
              <Link href={`/rating/${contentId}/${notice.moved}`} style={{ fontWeight: 700 }}>
                {SENTIMENT_NAME[notice.moved]} 탭에서 보기 ›
              </Link>
            </>
          )}
        </p>
      )}

      <ReviewForm
        // 내 후기를 찾으면 그 내용으로 폼을 다시 채운다
        key={myReview ? `mine-${myReview.id}-${myReview.updatedAt}` : "new"}
        contentId={contentId}
        sentiment={sentiment}
        myReview={myReview}
        askProgress={askProgress}
        onSaved={afterSave}
        onInvalid={(text) => setNotice({ text })}
      />
    </>
  );
}

function ReviewForm({
  contentId,
  sentiment,
  myReview,
  askProgress,
  onSaved,
  onInvalid,
}: {
  contentId: number;
  sentiment: Sentiment;
  myReview?: RatingReviewItem;
  askProgress: boolean;
  /** 저장에 성공하면 안내 문구와, 다른 탭으로 옮겨졌으면 그 탭을 넘긴다 */
  onSaved: (text: string, moved?: Sentiment) => Promise<void>;
  onInvalid: (text: string) => void;
}) {
  const [choice, setChoice] = useState<Sentiment>(
    (myReview?.sentiment.slug as Sentiment | undefined) ?? sentiment,
  );
  const [score, setScore] = useState(myReview?.score ?? 0);
  const [title, setTitle] = useState(myReview?.title ?? "");
  const [body, setBody] = useState(myReview?.body ?? "");
  const [progress, setProgress] = useState(myReview?.progress?.toString() ?? "");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<ApiError | null>(null);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (score < 1) {
      onInvalid("별점을 골라 주세요.");
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      const result = await apiFetch<RatingReviewSaveResult>(`/api/ratings/${contentId}/reviews`, {
        method: "POST",
        body: {
          sentiment: choice,
          score,
          title: title.trim(),
          body: body.trim(),
          progress: askProgress && progress !== "" ? Number(progress) : null,
        },
      });
      const saved = result.created
        ? result.rewardPoint > 0
          ? `후기를 등록했어요. ${result.rewardPoint}P가 적립됐어요!`
          : "후기를 등록했어요."
        : "후기를 수정했어요.";
      await onSaved(saved, choice !== sentiment ? choice : undefined);
    } catch (caught) {
      if (caught instanceof ApiError && caught.status !== 401) {
        setError(caught);
      }
    } finally {
      setSubmitting(false);
    }
  }

  const fieldError = (name: string) =>
    error?.fieldMessage(name) && (
      <span style={{ color: "#c00", fontSize: 12 }}>{error.fieldMessage(name)}</span>
    );

  return (
    <section
      aria-label="후기 남기기"
      style={{ marginTop: 32, padding: 24, border: "1px solid #222", borderRadius: 12 }}
    >
      <h2 style={{ fontSize: 19, fontWeight: 700 }}>{myReview ? "내 후기 수정" : "후기 남기기"}</h2>
      <p style={{ marginTop: 6, fontSize: 12, color: "#777" }}>
        콘텐츠마다 후기는 하나예요. 이미 쓴 후기가 있으면 수정돼요. 처음 쓰면 10P를 드려요.
      </p>

      {error && error.errors.length === 0 && (
        <p role="alert" style={{ marginTop: 14, fontSize: 13, color: "#c00" }}>
          {error.message}
        </p>
      )}

      <form onSubmit={submit} style={{ display: "flex", flexDirection: "column", gap: 16, marginTop: 20 }}>
        <div role="radiogroup" aria-label="좋아요/싫어요" style={{ display: "flex", gap: 8 }}>
          {(["like", "dislike"] as const).map((option) => (
            <button
              key={option}
              type="button"
              role="radio"
              aria-checked={choice === option}
              onClick={() => setChoice(option)}
              style={{
                flex: 1,
                padding: "10px 0",
                border: "1px solid #222",
                borderRadius: 6,
                background: choice === option ? "#111" : "#fff",
                color: choice === option ? "#fff" : "#111",
                fontSize: 14,
                fontWeight: 700,
                cursor: "pointer",
              }}
            >
              {option === "like" ? "👍" : "👎"} {SENTIMENT_NAME[option]}
            </button>
          ))}
        </div>

        <div role="radiogroup" aria-label="별점" style={{ display: "flex", alignItems: "center", gap: 4 }}>
          {[1, 2, 3, 4, 5].map((star) => (
            <button
              key={star}
              type="button"
              role="radio"
              aria-checked={score === star}
              aria-label={`${star}점`}
              onClick={() => setScore(star)}
              style={{
                border: "none",
                background: "none",
                fontSize: 26,
                lineHeight: 1,
                color: star <= score ? "#111" : "#ccc",
                cursor: "pointer",
              }}
            >
              ★
            </button>
          ))}
          <span style={{ marginLeft: 8, fontSize: 13, color: "#555" }}>
            {score > 0 ? `${score}점` : "별점을 골라 주세요"}
          </span>
          {fieldError("score")}
        </div>

        <label style={{ display: "flex", flexDirection: "column", gap: 6, fontSize: 13 }}>
          한줄 제목
          <input
            value={title}
            onChange={(event) => setTitle(event.target.value)}
            required
            maxLength={100}
            placeholder="예: 연출이 정말 좋아요"
            style={{ height: 40, padding: "0 10px", border: "1px solid #999", borderRadius: 6, fontSize: 14 }}
          />
          {fieldError("title")}
        </label>

        <label style={{ display: "flex", flexDirection: "column", gap: 6, fontSize: 13 }}>
          후기
          <textarea
            value={body}
            onChange={(event) => setBody(event.target.value)}
            required
            maxLength={BODY_MAX}
            rows={5}
            placeholder="어떤 점이 좋았는지, 아쉬웠는지 알려 주세요"
            style={{ padding: 10, border: "1px solid #999", borderRadius: 6, fontSize: 14, resize: "vertical" }}
          />
          <span style={{ alignSelf: "flex-end", fontSize: 11, color: "#777" }}>
            {body.length} / {BODY_MAX}
          </span>
          {fieldError("body")}
        </label>

        {askProgress && (
          <label style={{ display: "flex", flexDirection: "column", gap: 6, fontSize: 13 }}>
            몇 화까지 봤나요? (선택)
            <input
              type="number"
              min={0}
              inputMode="numeric"
              value={progress}
              onChange={(event) => setProgress(event.target.value)}
              placeholder="예: 8"
              style={{ width: 120, height: 40, padding: "0 10px", border: "1px solid #999", borderRadius: 6, fontSize: 14 }}
            />
            {fieldError("progress")}
          </label>
        )}

        <button
          type="submit"
          disabled={submitting}
          style={{
            alignSelf: "flex-end",
            padding: "12px 28px",
            border: "none",
            borderRadius: 6,
            background: "#111",
            color: "#fff",
            fontSize: 14,
            fontWeight: 700,
            cursor: submitting ? "wait" : "pointer",
            opacity: submitting ? 0.6 : 1,
          }}
        >
          {submitting ? "저장 중..." : myReview ? "수정하기" : "등록하기"}
        </button>
      </form>
    </section>
  );
}
