package com.commerce.pagopa.global.exception.handler;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ApiResponse;
import com.commerce.pagopa.global.response.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 커스텀 예외 (BusinessException 하위 전체 처리)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        log.warn("[BusinessException] code={}, message={}", e.getErrorCode().getCode(), e.getMessage());

        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.error(errorCode));
    }

    // @Valid 검증 실패 (@RequestBody)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getDefaultMessage())
                .findFirst()
                .orElse("입력 값이 올바르지 않습니다.");

        log.warn("[Validation] {}", message);
        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT_VALUE.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.INVALID_INPUT_VALUE, message));
    }

    // @Valid 검증 실패 (@RequestParam, @PathVariable)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException e) {
        log.warn("[ConstraintViolation] {}", e.getMessage());
        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT_VALUE.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.INVALID_INPUT_VALUE));
    }

    // 지원하지 않는 HTTP 메서드
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        log.warn("[MethodNotAllowed] {}", e.getMessage());
        return ResponseEntity
                .status(ErrorCode.METHOD_NOT_ALLOWED.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.METHOD_NOT_ALLOWED));
    }

    // JSON 타입 불일치
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("[MessageNotReadable] {}", e.getMessage());
        return ResponseEntity
                .status(ErrorCode.INVALID_TYPE_VALUE.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.INVALID_TYPE_VALUE));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e) {
        log.warn("[AccessDenied] {}", e.getMessage());
        return ResponseEntity
                .status(ErrorCode.ACCESS_DENIED.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.ACCESS_DENIED));
    }

    // 나머지 모든 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("[UnhandledException] {}", e.getMessage(), e);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
