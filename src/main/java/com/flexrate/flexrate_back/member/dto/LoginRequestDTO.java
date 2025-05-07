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
        @NotBlank(message = "이메일은 필수 항목입니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수 항목입니다.")
        String password,

        String passkeyData, // 패스키 인증 데이터를 받을 수 있도록 함 (FIDO2)
        String authMethod // 인증 방식(MFA or FIDO)
) {}