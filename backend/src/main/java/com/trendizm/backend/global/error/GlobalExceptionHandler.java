package com.trendizm.backend.global.error;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 컨트롤러에서 나온 예외를 모두 { code, message } 형식으로 바꾼다.
 * 인증 필터(Spring Security)에서 나는 401·403은 여기까지 오지 않으므로 SecurityConfig에서 같은 형식으로 맞춘다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 서비스가 의도해서 던진 예외 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.status()).body(ErrorResponse.of(errorCode, e.getMessage()));
    }

    /** @Valid 요청 본문 검증 실패. 어떤 필드가 왜 틀렸는지 errors에 담는다 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBody(MethodArgumentNotValidException e) {
        List<ErrorResponse.FieldError> errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorResponse.FieldError(error.getField(), error.getDefaultMessage()))
                .toList();
        return invalidInput(errors);
    }

    /** @RequestParam·@PathVariable 등 메서드 파라미터 검증 실패. field에는 파라미터 이름이 들어간다 */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidParameter(HandlerMethodValidationException e) {
        List<ErrorResponse.FieldError> errors = e.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> new ErrorResponse.FieldError(
                                result.getMethodParameter().getParameterName(), error.getDefaultMessage())))
                .toList();
        return invalidInput(errors);
    }

    /** JSON 형식 오류, 필수 파라미터 누락, 타입 불일치 */
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception e) {
        return respond(CommonErrorCode.INVALID_INPUT);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoResourceFoundException e) {
        return respond(CommonErrorCode.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        return respond(CommonErrorCode.METHOD_NOT_ALLOWED);
    }

    /** 컨트롤러 안에서 난 인증·권한 예외 (예: @PreAuthorize) */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException e) {
        return respond(CommonErrorCode.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
        return respond(CommonErrorCode.FORBIDDEN);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException e) {
        return respond(CommonErrorCode.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * 예상하지 못한 예외. 내부 내용은 응답에 보이지 않고 로그로만 남긴다.
     * 단, 위에서 따로 처리하지 않은 스프링 MVC 예외(헤더 누락, 406 등)는 스프링이 정한 4xx 상태를 그대로 쓴다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        if (e instanceof org.springframework.web.ErrorResponse springError
                && springError.getStatusCode().is4xxClientError()) {
            CommonErrorCode errorCode = CommonErrorCode.BAD_REQUEST;
            return ResponseEntity.status(springError.getStatusCode())
                    .body(ErrorResponse.of(errorCode));
        }
        log.error("처리하지 못한 예외", e);
        return respond(CommonErrorCode.INTERNAL_ERROR);
    }

    private ResponseEntity<ErrorResponse> invalidInput(List<ErrorResponse.FieldError> errors) {
        ErrorCode errorCode = CommonErrorCode.INVALID_INPUT;
        return ResponseEntity.status(errorCode.status()).body(ErrorResponse.of(errorCode, errors));
    }

    private ResponseEntity<ErrorResponse> respond(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.status()).body(ErrorResponse.of(errorCode));
    }
}
