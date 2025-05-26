package com.flexrate.flexrate_back.member.api;

import com.flexrate.flexrate_back.auth.domain.jwt.JwtTokenProvider;
import com.flexrate.flexrate_back.auth.domain.repository.PinCredentialRepository;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.application.LoginService;
import com.flexrate.flexrate_back.member.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PinCredentialRepository pinCredentialRepository;


    @Operation(
            summary = "PIN 로그인",
            description = "로컬스토리지 userId값과 PIN 6자리로 로그인",
            tags = { "Auth Controller" }
    )
    @PostMapping("/login/pin")
    public ResponseEntity<LoginResponseDTO> loginWithPin(
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody @Valid PinLoginRequestDTO request
    ) {
        String token = bearerToken.replace("Bearer ", "");
        LoginResponseDTO response = loginService.loginWithPin(token, request);
        return ResponseEntity.ok(response);
    }



    @Operation(
            summary = "PIN 등록",
            description = "userId값과 PIN 6자리룰 PIN 엔터티에 저장",
            tags = { "Auth Controller" }
    )
    @PostMapping("/login/pin/register")
    public ResponseEntity<String> registerPin(@RequestBody @Valid PinRegisterRequestDTO request,
                                              Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        loginService.registerPin(request, userId);
        return ResponseEntity.ok("PIN 등록 완료");
    }


    @Operation(
            summary = "PIN 등록 조회",
            description = "로컬스토리지 userId로 PIN 엔터티에 저장된 memberId를 조회",
            tags = { "Auth Controller" }
    )
    @GetMapping("/login/pin/registered")
    public ResponseEntity<Boolean> checkPinRegistered(@RequestHeader(value = "Authorization", required = false) String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            log.error("[ERROR] Authorization header is missing or invalid: {}", bearerToken);
            throw new FlexrateException(ErrorCode.USER_NOT_FOUND);
        }
        String token = bearerToken.replace("Bearer ", "");
        Long userId;
        try {
            userId = jwtTokenProvider.getMemberId(token);
            log.info("userId extracted from token: {}", userId);
        } catch (Exception e) {
            log.error("Failed to extract userId from token", e);
            throw new FlexrateException(ErrorCode.VALIDATION_ERROR);
        }

        boolean isRegistered = loginService.isPinRegistered(userId);
        log.info("PIN 등록 여부 조회 결과: {}", isRegistered);
        return ResponseEntity.ok(isRegistered);
    }

    @Operation(
            summary = "패스워드 로그인",
            description = "이메일과 비밀번호를 사용하여 로그인합니다.",
            tags = { "Auth Controller" }
    )
    @PostMapping("/login/password")
    public ResponseEntity<LoginResponseDTO> loginWithPassword(@RequestBody @Valid PasswordLoginRequestDTO request) {
        LoginResponseDTO response = loginService.loginWithPassword(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Passkey 로그인",
            description = "Passkey를 사용하여 로그인합니다.",
            tags = { "Auth Controller" }
    )
    @PostMapping("/login/passkey")
    public ResponseEntity<LoginResponseDTO> loginWithPasskey(@RequestBody @Valid PasskeyLoginRequestDTO request) {
        LoginResponseDTO response = loginService.loginWithPasskey(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "MFA 로그인",
            description = "다중 인증(MFA)을 사용하여 로그인합니다.",
            tags = { "Auth Controller" }
    )
    @PostMapping("/login/mfa")
    public ResponseEntity<LoginResponseDTO> loginWithMfa(@RequestBody @Valid MfaLoginRequestDTO request) {
        LoginResponseDTO response = loginService.loginWithMFA(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "로그아웃",
            description = "로그인된 사용자의 로그아웃을 처리하며, refreshToken을 삭제합니다.",
            tags = { "Auth Controller" }
    )
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String refreshToken) {
        loginService.logout(refreshToken);
        return ResponseEntity.ok("로그아웃");
    }
}