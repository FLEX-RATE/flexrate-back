package com.flexrate.flexrate_back.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * FLEXRATE 커스텀 예외
 * @since 2024.04.25
 */
@Getter
public class FlexrateException extends RuntimeException {
    private final ErrorCode errorCode;

    public FlexrateException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public FlexrateException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public String getCode() {
        return errorCode.getCode();
    }

    @Override
    public String getMessage() {
        return errorCode.getMessage();
    }
}
