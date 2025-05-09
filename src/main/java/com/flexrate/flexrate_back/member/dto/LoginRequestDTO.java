package com.flexrate.flexrate_back.member.dto;

import com.flexrate.flexrate_back.auth.enums.AuthMethod;
import com.flexrate.flexrate_back.auth.enums.AuthResult;
import com.flexrate.flexrate_back.auth.enums.MfaType;
import com.flexrate.flexrate_back.member.enums.LoginMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 로그인 요청
 * @since 2025.05.05
 * @author 윤영찬
 */
@Builder
@Schema(description = "로그인 요청 DTO")
public record LoginRequestDTO(

        @NotBlank(message = "이메일은 필수 항목입니다.")
        String email,
        String password,
        String passkeyData,
        LoginMethod loginMethod,
        AuthMethod authMethod,
        MfaType mfaType,
        String deviceInfo,
        AuthResult mfaResult,
        String challenge

) {}