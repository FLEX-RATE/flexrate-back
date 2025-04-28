package com.flexrate.flexrate_back.common.exception;

import com.flexrate.flexrate_back.common.util.ProfileUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final ProfileUtil profileUtil;

    /**
     * Validation 에러 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        ErrorResponse errorResponse = new ErrorResponse("V001", String.join(", ", errors));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


    /**
     * 404 에러 처리
     */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(
            org.springframework.web.servlet.resource.NoResourceFoundException ex,
            HttpServletRequest request) {
        String code = ErrorCode.NOT_FOUND_STATIC_RESOURCE.getCode();
        String message = ErrorCode.NOT_FOUND_STATIC_RESOURCE.getMessage();
        String path = request.getRequestURI();

        log.warn("[{}] {} | path={} | detail={}", code, message, path, ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(code, message));
    }


    /**
     * 커스텀 예외 처리
     */
    @ExceptionHandler(FlexrateException.class)
    public ResponseEntity<ErrorResponse> handleFlexrateException(FlexrateException ex, HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();
        String code = errorCode.getCode();
        String message = errorCode.getMessage();
        String path = request.getRequestURI();

        if (profileUtil.isProduction()) {
            log.error("[{}] {} | path={}", code, message, path);
        } else {
            log.error("[{}] {} | path={} | detail={}", code, message, path, ex.getMessage());
        }

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
        if (profileUtil.isProduction()) {
            log.error("Unhandled exception occurred: {}", ex.toString());
        } else {
            log.error("Unhandled exception occurred", ex);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
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
