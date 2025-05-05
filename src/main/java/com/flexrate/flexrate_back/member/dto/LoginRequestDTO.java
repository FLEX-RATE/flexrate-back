package com.flexrate.flexrate_back.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/*
 * 로그인 요청
 * @since 2025.05.05
 * @author 윤영찬
 */
public record LoginRequestDTO(
        @Email(message = "이메일을 입력해 주세요.")
        @NotBlank(message = "이메일은 필수 항목입니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수 항목입니다.")
        String password,

        Long userId,
        String passkeyData
) {}
