package com.flexrate.flexrate_back.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @Email(message = "유효한 이메일을 입력해 주세요.")
        @NotBlank(message = "이메일은 필수 항목입니다.")
        String email,

        String password,
        String passkeyId
) {}
