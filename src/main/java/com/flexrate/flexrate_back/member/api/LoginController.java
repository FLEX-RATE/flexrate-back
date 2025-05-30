package com.flexrate.flexrate_back.member.api;

import com.flexrate.flexrate_back.member.application.LoginService;
import com.flexrate.flexrate_back.member.dto.LoginResponseDTO;
import com.flexrate.flexrate_back.member.dto.MfaLoginRequestDTO;
import com.flexrate.flexrate_back.member.dto.PasskeyLoginRequestDTO;
import com.flexrate.flexrate_back.member.dto.PasswordLoginRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @Operation(
            summary = "패스워드 로그인",
            description = "이메일과 비밀번호를 사용하여 로그인합니다.",
            tags = { "Auth Controller" }
    )
    @PostMapping("/login/password")
    public ResponseEntity<LoginResponseDTO> loginWithPassword(
            @RequestBody @Valid PasswordLoginRequestDTO request,
            HttpServletResponse response
    ) {
        LoginResponseDTO loginResponse = loginService.loginWithPassword(request);

        String refreshToken = loginResponse.refreshToken();

        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(false) // <---- https일 시, true로 변경하기!
                .path("/")
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(loginResponse);
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