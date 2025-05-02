package com.flexrate.flexrate_back.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "에러 응답 DTO")
public class ErrorResponse {

    @Schema(description = "에러 코드", example = "U001")
    private String code;

    @Schema(description = "에러 메시지", example = "사용자를 찾을 수 없습니다")
    private String message;
}
