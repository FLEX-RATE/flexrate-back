package com.flexrate.flexrate_back.common.exception;

import com.flexrate.flexrate_back.common.util.ProfileUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final ProfileUtil profileUtil;

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
        HttpStatus status = switch (ex.getCode()) {
            case "A001" -> HttpStatus.CONFLICT;
            case "A000", "A002", "A003", "A004", "M001", "M002" -> HttpStatus.UNAUTHORIZED;
            case "L001", "L003", "L004", "P001" -> HttpStatus.BAD_REQUEST;
            case "L002", "P002", "U001", "S404" -> HttpStatus.NOT_FOUND;
            case "S500" -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.BAD_REQUEST;
        };

        String code = ex.getCode();
        String message = ex.getMessage();
        String path = request.getRequestURI();

        if (profileUtil.isProduction()) {
            log.error("[{}] {} | path={}", code, message, path);
        } else {
            log.error("[{}] {} | path={} | detail={} ㅋㅋ", code, message, path, ex.getMessage());
        }

        return ResponseEntity.status(status)
                .body(new ErrorResponse(ex.getCode(), ex.getMessage()));
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
