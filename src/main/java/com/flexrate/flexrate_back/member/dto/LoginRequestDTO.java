package com.flexrate.flexrate_back.member.dto;

import com.flexrate.flexrate_back.member.enums.LoginMethod;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/*
 * 로그인 요청
 * @since 2025.05.05
 * @author 윤영찬
 */
@Builder
public record LoginRequestDTO(
        @Email(message = "유효한 이메일을 입력해주세요.")
        @NotBlank(message = "이메일은 필수 항목입니다.")
        String email,

        @NotNull(message = "비밀번호를 입력하세요")
        String password,

        LoginMethod method,

        String passkeyData
) {}
