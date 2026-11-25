"use client";

import { useRouter } from "next/navigation";
import { useEffect, useRef, useState, type CSSProperties } from "react";
import { apiFetch } from "@/lib/api/client";
import { ApiError } from "@/lib/api/types";

// 와이어프레임에는 없고 WBS 11.2·11.6(헤더 알림 UI)으로 만든다. 로그인했을 때만 SiteHeader가 그린다.
// - 🔔 옆에 안 읽은 알림 수를 보이고, 누르면 최근 알림 10개를 펼친다. 알림을 누르면 읽음 처리하고 해당 밈 화면으로 간다.
// - 안 읽은 수는 SSE(GET /api/notifications/stream)로 실시간으로 받는다. 서버는 수만 보내므로 목록은 펼칠 때 API로 부른다.
// - SSE가 끊기면 apiFetch로 수를 한 번 다시 불러 토큰을 재발급받고(EventSource는 재발급을 못 한다) 다시 연결한다.
//   그 요청이 401이면 로그아웃된 것이라 연결을 멈춘다. 401만 계속 반복해서 재연결하지 않게 하려는 것이다.
// - 탭이 많아 서버가 이 연결을 밀어내면("evicted") 다시 연결하지 않는다. 다시 연결하면 다른 탭을 밀어내고,
//   그 탭이 또 다시 연결하는 일이 반복된다. 이 탭을 다시 보면(visibilitychange) 그때 연결한다.

/** GET /api/notifications의 한 줄 (backend NotificationResponse) */
type NotificationItem = {
  id: number;
  /** "meme-vote"(내 해석에 동의) 또는 "meme-interpretation"(내 문서에 새 해석) */
  type: string;
  memeId: number;
  interpretationId: number;
  /** 밈 단어 */
  subject: string;
  count: number;
  message: string;
  read: boolean;
  occurredAt: string;
};

type NotificationPage = { content: NotificationItem[] };
type UnreadCount = { count: number };

const STREAM_PATH = "/api/notifications/stream";
const LIST_SIZE = 10;
/** 다시 연결할 때 기다리는 시간. 실패가 이어지면 두 배씩 늘려 최대 1분 */
const RETRY_MIN_MS = 2_000;
const RETRY_MAX_MS = 60_000;

/** 화면에는 안 보이고 스크린 리더만 읽는 글자 */
const VISUALLY_HIDDEN: CSSProperties = {
  position: "absolute",
  width: 1,
  height: 1,
  overflow: "hidden",
  clip: "rect(0 0 0 0)",
  whiteSpace: "nowrap",
};

/**
 * 알림을 누르면 갈 화면. 밈 화면 주소(/lookup/[slug]/[id])의 slug는 밈 단어다.
 * 주소 규칙을 백엔드에 두지 않고 여기서 만든다. 밈 화면 주소가 바뀌면 여기만 고친다.
 */
function notificationHref(item: NotificationItem): string {
  return `/lookup/${encodeURIComponent(item.subject)}/${item.interpretationId}`;
}

/** "방금 전", "5분 전", "3시간 전", "2일 전", 그 이상은 날짜 */
function timeAgo(iso: string, now: number): string {
  const minutes = Math.floor((now - new Date(iso).getTime()) / 60_000);
  if (minutes < 1) return "방금 전";
  if (minutes < 60) return `${minutes}분 전`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}시간 전`;
  const days = Math.floor(hours / 24);
  if (days < 7) return `${days}일 전`;
  return new Date(iso).toLocaleDateString("ko-KR");
}

/** 안 읽은 알림 수와, SSE로 값을 받을 때마다 1씩 오르는 version */
export type UnreadState = {
  /** null: 아직 모름(숫자를 그리지 않는다) */
  count: number | null;
  /**
   * 같은 해석의 동의는 알림 하나로 묶여서 안 읽은 수가 그대로일 수 있다("동의 3개" → "동의 4개").
   * 그래도 서버는 이벤트를 보내므로, 펼친 목록은 수 대신 이 값이 바뀔 때 다시 불러온다.
   */
  version: number;
};

/**
 * 안 읽은 알림 수. SSE로 받고, 끊기면 재발급을 거쳐 다시 연결한다.
 * 헤더(SiteHeader)에서 한 번만 부르고 값을 🔔(데스크톱·모바일)에 넘긴다. 🔔마다 부르면 탭 하나가 연결을 2개 연다.
 * - 연결마다 지역 변수(current)로 닫는다. 공유 변수(source)는 "지금 살아 있는 연결"을 가리킬 때만 쓴다.
 * - 다시 연결하는 중(refreshing)이거나 이미 연결돼 있으면 또 연결하지 않는다. 재연결을 기다리는 동안 탭을 오가도
 *   연결이 하나만 남는다.
 * - 처음 연결이 실패하면(페이지를 열 때 토큰이 이미 만료된 경우 등) 기다리지 않고 바로 재발급 후 다시 연결한다.
 * @param enabled 로그인했을 때만 true. false면 연결하지 않는다
 */
export function useUnreadCount(enabled: boolean): UnreadState {
  const [state, setState] = useState<UnreadState>({ count: null, version: 0 });

  useEffect(() => {
    if (!enabled) return;
    let source: EventSource | null = null;
    let retryTimer: ReturnType<typeof setTimeout> | undefined;
    let retryDelay = RETRY_MIN_MS;
    let firstAttempt = true;
    let refreshing = false;
    let stopped = false;

    function receive(count: number) {
      setState((previous) => ({ count, version: previous.version + 1 }));
    }

    function connect() {
      if (stopped || source !== null) return;
      const current = new EventSource(STREAM_PATH);
      source = current;
      current.addEventListener("unread-count", (event) => {
        retryDelay = RETRY_MIN_MS;
        receive((JSON.parse((event as MessageEvent<string>).data) as UnreadCount).count);
      });
      current.addEventListener("evicted", () => {
        current.close();
        if (source === current) source = null;
      });
      // 끊기면 브라우저의 자동 재연결 대신 직접 다시 연결한다(그 사이에 토큰 재발급을 넣기 위해)
      current.onerror = () => {
        current.close();
        if (source !== current) return;
        source = null;
        scheduleReconnect(firstAttempt ? 0 : retryDelay);
        firstAttempt = false;
      };
    }

    function scheduleReconnect(delay: number) {
      if (stopped || retryTimer !== undefined) return;
      retryTimer = setTimeout(() => {
        retryTimer = undefined;
        void refreshThenConnect();
      }, delay);
      retryDelay = Math.min(retryDelay * 2, RETRY_MAX_MS);
    }

    async function refreshThenConnect() {
      if (stopped || refreshing || source !== null) return;
      refreshing = true;
      try {
        // 401이면 apiFetch가 재발급을 시도한다. 그래도 401이면 로그아웃된 것이라 멈춘다
        const latest = await apiFetch<UnreadCount>("/api/notifications/unread-count", {
          redirectOnUnauthorized: false,
        });
        if (stopped) return;
        receive(latest.count);
        connect();
      } catch (error) {
        if (error instanceof ApiError && error.status === 401) {
          stopped = true;
          return;
        }
        scheduleReconnect(retryDelay);
      } finally {
        refreshing = false;
      }
    }

    // 다른 탭에 있다가 돌아왔을 때 끊겨 있으면(밀려난 경우 포함) 바로 다시 연결한다
    function onVisible() {
      if (document.visibilityState === "visible" && !stopped && source === null && !refreshing) {
        clearTimeout(retryTimer);
        retryTimer = undefined;
        void refreshThenConnect();
      }
    }

    connect();
    document.addEventListener("visibilitychange", onVisible);
    return () => {
      stopped = true;
      clearTimeout(retryTimer);
      source?.close();
      document.removeEventListener("visibilitychange", onVisible);
    };
  }, [enabled]);

  return state;
}

/** 🔔와 펼친 알림 목록. 안 읽은 수(unread)는 헤더가 useUnreadCount로 받아 넘긴다 */
export default function NotificationBell({ unread }: { unread: UnreadState }) {
  const router = useRouter();
  const unreadCount = unread.count;
  const [open, setOpen] = useState(false);
  const [items, setItems] = useState<NotificationItem[] | null>(null);
  /** 목록을 불러온 시각. "5분 전" 같은 표시의 기준이다 */
  const [loadedAt, setLoadedAt] = useState(0);
  const [error, setError] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  // 펼칠 때, 그리고 펼친 채로 새 알림이 오면(SSE를 받을 때마다 version이 오른다) 목록을 다시 부른다
  useEffect(() => {
    if (!open) return;
    let active = true;
    apiFetch<NotificationPage>(`/api/notifications?size=${LIST_SIZE}`)
      .then((page) => {
        if (!active) return;
        setItems(page.content);
        setLoadedAt(Date.now());
        setError(false);
      })
      .catch(() => active && setError(true));
    return () => {
      active = false;
    };
  }, [open, unread.version]);

  // 바깥을 누르거나 Esc를 누르면 닫는다
  useEffect(() => {
    if (!open) return;
    function onPointerDown(event: PointerEvent) {
      if (!containerRef.current?.contains(event.target as Node)) {
        setOpen(false);
      }
    }
    function onKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") {
        setOpen(false);
      }
    }
    document.addEventListener("pointerdown", onPointerDown);
    document.addEventListener("keydown", onKeyDown);
    return () => {
      document.removeEventListener("pointerdown", onPointerDown);
      document.removeEventListener("keydown", onKeyDown);
    };
  }, [open]);

  function openNotification(item: NotificationItem) {
    setOpen(false);
    if (!item.read) {
      // 읽음 처리를 기다리지 않고 바로 이동한다(서버가 느려도 이동은 바로). 실패해도 이동은 하고,
      // 로그인 화면으로 보내지 않는다. 안 읽은 수는 읽음 처리가 끝나면 서버가 SSE로 다시 보내 준다
      void apiFetch(`/api/notifications/${item.id}/read`, { method: "PATCH", redirectOnUnauthorized: false }).catch(
        () => {},
      );
    }
    router.push(notificationHref(item));
  }

  const badge = unreadCount === null || unreadCount === 0 ? null : unreadCount > 99 ? "99+" : String(unreadCount);

  return (
    <div ref={containerRef} style={{ position: "relative" }}>
      <button
        type="button"
        aria-label={badge ? `알림, 안 읽은 알림 ${unreadCount}개` : "알림"}
        aria-haspopup="true"
        aria-expanded={open}
        onClick={() => setOpen((value) => !value)}
        style={{
          position: "relative",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          width: 32,
          height: 32,
          border: "none",
          background: "none",
          fontSize: 18,
          cursor: "pointer",
        }}
      >
        <span aria-hidden="true">🔔</span>
        {badge && (
          <span
            aria-hidden="true"
            style={{
              position: "absolute",
              top: 0,
              right: -4,
              minWidth: 16,
              height: 16,
              padding: "0 4px",
              borderRadius: 8,
              background: "#c62828",
              color: "#fff",
              fontSize: 10,
              fontWeight: 700,
              lineHeight: "16px",
              textAlign: "center",
            }}
          >
            {badge}
          </span>
        )}
      </button>

      {open && (
        <div
          role="region"
          aria-label="알림 목록"
          style={{
            position: "absolute",
            top: 40,
            right: 0,
            zIndex: 20,
            width: 300,
            maxWidth: "calc(100vw - 20px)",
            border: "1px solid #999",
            borderRadius: 3,
            background: "#fff",
            color: "#111",
            boxShadow: "0 4px 12px rgba(0, 0, 0, 0.12)",
          }}
        >
          <p style={{ padding: "12px 14px", borderBottom: "1px solid #ddd", fontSize: 13, fontWeight: 700 }}>알림</p>
          {error ? (
            <p style={{ padding: "16px 14px", fontSize: 12, color: "#c62828" }}>알림을 불러오지 못했습니다.</p>
          ) : items === null ? (
            <p style={{ padding: "16px 14px", fontSize: 12, color: "#555" }}>불러오는 중…</p>
          ) : items.length === 0 ? (
            <p style={{ padding: "16px 14px", fontSize: 12, color: "#555" }}>새 알림이 없습니다.</p>
          ) : (
            <ul style={{ maxHeight: 360, overflowY: "auto", listStyle: "none" }}>
              {items.map((item) => (
                <li key={item.id} style={{ borderBottom: "1px solid #eee" }}>
                  <button
                    type="button"
                    onClick={() => openNotification(item)}
                    style={{
                      display: "flex",
                      gap: 8,
                      width: "100%",
                      padding: "12px 14px",
                      border: "none",
                      background: item.read ? "#fff" : "#f5f7ff",
                      textAlign: "left",
                      cursor: "pointer",
                    }}
                  >
                    <span
                      aria-hidden="true"
                      style={{
                        flex: "none",
                        width: 6,
                        height: 6,
                        marginTop: 6,
                        borderRadius: 3,
                        background: item.read ? "transparent" : "#1a56db",
                      }}
                    />
                    <span style={{ display: "flex", flexDirection: "column", gap: 4 }}>
                      <span style={{ fontSize: 13, lineHeight: 1.5, color: item.read ? "#555" : "#111" }}>
                        {item.read ? null : <span style={VISUALLY_HIDDEN}>안 읽음: </span>}
                        {item.message}
                      </span>
                      <span style={{ fontSize: 11, color: "#777" }}>{timeAgo(item.occurredAt, loadedAt)}</span>
                    </span>
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}
