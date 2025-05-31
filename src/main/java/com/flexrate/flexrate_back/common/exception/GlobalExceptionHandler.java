package com.flexrate.flexrate_back.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    /**
     * 필수 파라미터 누락 에러 처리
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String code = ErrorCode.AUTH_REQUIRED_FIELD_MISSING.getCode();
        String message = ErrorCode.AUTH_REQUIRED_FIELD_MISSING.getMessage();
        initMDC(code, ex);
        logError(message);

        return buildResponse(HttpStatus.BAD_REQUEST, code, message);
    }

    /**
     * Validation 에러 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String code = ErrorCode.VALIDATION_ERROR.getCode();
        String message = ErrorCode.VALIDATION_ERROR.getMessage();
        initMDC(code, ex);

        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + ", " + b).orElse("");
        logWarn(message, details);

        return buildResponse(HttpStatus.BAD_REQUEST, code, details);
    }

    /**
     * 404 에러 처리
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex, HttpServletRequest request) {
        String code = ErrorCode.NOT_FOUND_STATIC_RESOURCE.getCode();
        String message = ErrorCode.NOT_FOUND_STATIC_RESOURCE.getMessage();
        String path = request.getRequestURI();
        initMDC(code, ex);

        log.error("{}: \npath={}\ndetails={}",
                message,
                path,
                ex.getStackTrace());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(code, message));
    }


    /**
     * 커스텀 예외 처리
     */
    @ExceptionHandler(FlexrateException.class)
    public ResponseEntity<ErrorResponse> handleFlexrateException(FlexrateException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        String code = errorCode.getCode();
        String message = errorCode.getMessage();
        initMDC(code, ex);
        logError(message);

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(new ErrorResponse(code, message));
    }

    /**
     * 예상치 못한 예외 처리
     * - 예외 전체 로그
     * - 보안상 노출 위험 민감 필드는 별도 처리
     * - 운영 환경: 메시지만, 개발 환경: 전체 스택 트레이스
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        String code = ErrorCode.INTERNAL_SERVER_ERROR.getCode();
        String message = ErrorCode.INTERNAL_SERVER_ERROR.getMessage();
        initMDC(code, ex);
        logError(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(code, message));
    }

    /**
     * 공통 헬퍼
     * - errorCode, message, MDC pageId, stackTrace
     */
    private void initMDC(String code, Exception ex) {
        MDC.put("errorCode", code);
        MDC.put("details", ExceptionUtils.getStackTrace(ex));
    }

    private void logError(String message) {
        log.error("{}", message);
    }

    private void logWarn(String message, String details) {
        log.warn("{}: \n{}", message, details);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, message));
    }

    /**
     * 에러 응답 DTO (내부 클래스)
     */
    @Getter
    @AllArgsConstructor
    public static class ErrorResponse {
        private String code;
        private String message;
    }
}
