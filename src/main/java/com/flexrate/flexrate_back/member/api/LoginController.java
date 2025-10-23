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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @Value("${cookie.secure:false}")
    private boolean cookieSecure;

    @Operation(
            summary = "패스워드 로그인",
            description = "이메일과 비밀번호를 사용하여 로그인합니다."
    )
    @PostMapping("/login/password")
    public ResponseEntity<LoginResponseDTO> loginWithPassword(
            @RequestBody @Valid PasswordLoginRequestDTO request,
            HttpServletResponse response
    ) {
        LoginResponseDTO loginResponse = loginService.loginWithPassword(request, response);

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", loginResponse.refreshToken())
                .httpOnly(true).secure(cookieSecure).path("/").sameSite("Lax").build();

        ResponseCookie accessCookie = ResponseCookie.from("access_token", loginResponse.accessToken())
                .httpOnly(true).secure(cookieSecure).path("/").sameSite("Strict")
                .maxAge(60 * 60 * 2).build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

        return ResponseEntity.ok(loginResponse);
    }


    @Operation(
            summary = "Passkey 로그인",
            description = "Passkey를 사용하여 로그인합니다."
    )
    @PostMapping("/login/passkey")
    public ResponseEntity<LoginResponseDTO> loginWithPasskey(@RequestBody @Valid PasskeyLoginRequestDTO request, HttpServletResponse response) {
        LoginResponseDTO loginResponse = loginService.loginWithPasskey(request, response);
        return ResponseEntity.ok(loginResponse);
    }

    @Operation(
            summary = "MFA 로그인",
            description = "다중 인증(MFA)을 사용하여 로그인합니다."
    )
    @PostMapping("/login/mfa")
    public ResponseEntity<LoginResponseDTO> loginWithMfa(@RequestBody @Valid MfaLoginRequestDTO request, HttpServletResponse response) {
        LoginResponseDTO loginResponse = loginService.loginWithMFA(request, response);
        return ResponseEntity.ok(loginResponse);
    }

    @Operation(
            summary = "로그아웃",
            description = "로그인된 사용자의 로그아웃을 처리하며, refreshToken을 삭제합니다."
    )

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @CookieValue(value = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken != null) {
            loginService.logout(refreshToken);
        }

        ResponseCookie delRefresh = ResponseCookie.from("refresh_token", "")
                .httpOnly(true).secure(cookieSecure).path("/").maxAge(0).sameSite("Lax").build();
        ResponseCookie delAccess  = ResponseCookie.from("access_token", "")
                .httpOnly(true).secure(cookieSecure).path("/").maxAge(0).sameSite("Lax").build();

        response.addHeader(HttpHeaders.SET_COOKIE, delRefresh.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, delAccess.toString());

        return ResponseEntity.ok("로그아웃");
    }

}