package com.flexrate.flexrate_back.member.dto;

import com.flexrate.flexrate_back.auth.enums.AuthMethod;
import com.flexrate.flexrate_back.member.enums.LoginMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "Passkey 로그인 요청 DTO")
public record PasskeyLoginRequestDTO(

        @NotBlank(message = "이메일은 필수 항목입니다.")
        String email,

        PasskeyAuthenticationDTO passkeyData,

        LoginMethod loginMethod,

        AuthMethod authMethod,

        String deviceInfo,

        String challenge
) {}