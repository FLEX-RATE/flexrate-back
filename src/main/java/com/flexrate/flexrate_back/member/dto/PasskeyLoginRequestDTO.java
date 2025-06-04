package com.flexrate.flexrate_back.member.dto;

import com.flexrate.flexrate_back.auth.enums.AuthMethod;
import com.flexrate.flexrate_back.member.enums.LoginMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/*
* 이미 등록된 패스키를 서버에 저장과 인증 시 사용하는 인증 관련 정보를 담는 DTO
* date 2025.06.04
* author 윤영찬
* */

@Builder
@Schema(description = "Passkey 로그인 요청 DTO")
public record PasskeyLoginRequestDTO(

        @NotBlank(message = "이메일은 필수 항목입니다.")
        String email,

        PasskeyAuthenticationDTO passkeyData,

        AuthMethod authMethod,

        String deviceInfo,

        String challenge
) {}