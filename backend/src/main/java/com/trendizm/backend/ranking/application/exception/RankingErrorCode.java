package com.trendizm.backend.ranking.application.exception;

import com.trendizm.backend.global.error.ErrorCode;
import org.springframework.http.HttpStatus;

/** 트렌드 순위 도메인 에러. 예: throw new BusinessException(RankingErrorCode.RANKING_PLATFORM_NOT_FOUND); */
public enum RankingErrorCode implements ErrorCode {

    /** Platform에 없는 slug. 예: platform=naver */
    RANKING_PLATFORM_NOT_FOUND(HttpStatus.NOT_FOUND, "플랫폼을 찾을 수 없습니다."),

    /** trend_ranking_categories에 없거나 조회 응답에서 빼는 미분류 카테고리 id */
    RANKING_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."),

    /** 순위 CSV 업로드: 문법·열·값이 틀린 줄이 있음. 줄마다 이유는 응답의 errors에 담는다 */
    RANKING_CSV_INVALID(HttpStatus.BAD_REQUEST, "CSV에 잘못된 줄이 있습니다. errors에서 줄 번호와 이유를 확인해 주세요."),

    /** 순위 CSV 업로드: 본문이 RankingCsvReader.MAX_BYTES(5MB)보다 큼 */
    RANKING_CSV_TOO_LARGE(HttpStatus.CONTENT_TOO_LARGE,"CSV가 너무 큽니다. 5MB 이하로 올려 주세요."),

    /** 순위 CSV 업로드: 같은 플랫폼·카테고리·수집 시각의 스냅샷이 이미 있음 (같은 수집 결과를 다시 올림) */
    RANKING_SNAPSHOT_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 올린 수집 결과입니다.");

    private final HttpStatus status;
    private final String message;

    RankingErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public HttpStatus status() {
        return status;
    }

    @Override
    public String code() {
        return name();
    }

    @Override
    public String message() {
        return message;
    }
}
