// 백엔드 공통 응답 형식. 노션 WBS 1.7 "API 명세 작성"과 같다.

/** 필드별 입력값 오류. 400 INVALID_INPUT일 때만 온다 */
export type FieldError = {
  field: string;
  message: string;
};

/** 모든 실패 응답의 본문 */
export type ErrorResponse = {
  code: string;
  message: string;
  errors?: FieldError[];
};

/** 목록 응답. page는 0부터 시작한다 */
export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
};

/**
 * API 호출이 실패하면 던지는 에러.
 * 예: catch (e) { if (e instanceof ApiError && e.code === "DUPLICATE_EMAIL") ... }
 */
export class ApiError extends Error {
  readonly status: number;
  readonly code: string;
  readonly errors: FieldError[];

  constructor(status: number, body: ErrorResponse) {
    super(body.message);
    this.name = "ApiError";
    this.status = status;
    this.code = body.code;
    this.errors = body.errors ?? [];
  }

  /** 특정 필드의 오류 메시지. 입력칸 아래에 띄울 때 쓴다. 예: error.fieldMessage("email") */
  fieldMessage(field: string): string | undefined {
    return this.errors.find((error) => error.field === field)?.message;
  }
}

/**
 * 화면의 페이지 번호(1부터)를 API의 page(0부터)로 바꾼다. 예: ?page=3 → page=2
 * 주소창의 ?page=abc처럼 숫자가 아니거나 1보다 작으면 첫 페이지(0)로 본다.
 */
export function toApiPage(uiPage: number | string | null | undefined): number {
  const page = Math.floor(Number(uiPage));
  return Number.isFinite(page) ? Math.max(page - 1, 0) : 0;
}

/** API의 page(0부터)를 화면의 페이지 번호(1부터)로 바꾼다 */
export function toUiPage(apiPage: number): number {
  return apiPage + 1;
}

/** 서버에 닿지 못했을 때(네트워크 끊김, 백엔드 꺼짐 등) 던지는 에러. status는 0이다 */
export function networkError(): ApiError {
  return new ApiError(0, {
    code: "NETWORK_ERROR",
    message: "서버에 연결할 수 없습니다. 네트워크 상태를 확인해 주세요.",
  });
}

/** fetch를 실행하고, 네트워크 오류(TypeError)는 ApiError(NETWORK_ERROR)로 바꾼다. 요청 취소(AbortError)는 그대로 던진다 */
export async function fetchOrNetworkError(input: string, init: RequestInit): Promise<Response> {
  try {
    return await fetch(input, init);
  } catch (error) {
    if (error instanceof DOMException && error.name === "AbortError") {
      throw error;
    }
    throw networkError();
  }
}

/** 응답 본문을 ErrorResponse로 읽는다. JSON이 아니면(프록시 오류 등) 기본 메시지를 쓴다 */
export async function readErrorResponse(response: Response): Promise<ErrorResponse> {
  try {
    const body = (await response.json()) as Partial<ErrorResponse>;
    if (typeof body.code === "string" && typeof body.message === "string") {
      return { code: body.code, message: body.message, errors: body.errors };
    }
  } catch {
    // JSON이 아닌 응답
  }
  return {
    code: response.status >= 500 ? "INTERNAL_ERROR" : "UNKNOWN_ERROR",
    message: "요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.",
  };
}
