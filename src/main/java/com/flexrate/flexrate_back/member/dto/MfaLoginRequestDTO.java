package com.flexrate.flexrate_back.member.dto;

import com.flexrate.flexrate_back.auth.enums.AuthMethod;
import com.flexrate.flexrate_back.auth.enums.AuthResult;
import com.flexrate.flexrate_back.auth.enums.MfaType;
import com.flexrate.flexrate_back.member.enums.LoginMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "MFA 로그인 요청 DTO")
public record MfaLoginRequestDTO(

        @NotBlank(message = "이메일은 필수 항목입니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수 항목입니다.")
        String password, // 비밀번호

        LoginMethod loginMethod,

        AuthMethod authMethod,

        MfaType mfaType,

        AuthResult mfaResult,

        String deviceInfo,

        String challenge
) {}
