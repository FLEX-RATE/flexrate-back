package com.flexrate.flexrate_back.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PinRequest(
        @NotBlank(message = "pin은 필수 항목입니다.")
        @Pattern(regexp = "\\d{6}", message = "PIN은 6자리 숫자여야 합니다.")
        String pin
) {}
