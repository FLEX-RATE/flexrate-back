package com.flexrate.flexrate_back.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/*
 * 패스키 정보 전달 DTO
 * @since 2025.05.07
 * @author 윤영찬
 */
@Builder
public record PasskeyRequestDTO(
        @NotBlank(message = "Credential ID는 필수 항목입니다.")
        String credentialId,

        @NotBlank(message = "Public Key는 필수 항목입니다.")
        String publicKey,

        @NotNull(message = "Sign Count는 필수 항목입니다.")
        int signCount,

        @NotBlank(message = "Device Info는 필수 항목입니다.")
        String deviceInfo,

        boolean isActive
) {}
