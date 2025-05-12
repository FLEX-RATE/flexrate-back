package com.flexrate.flexrate_back.member.api;

import com.flexrate.flexrate_back.member.application.LoginService;
import com.flexrate.flexrate_back.member.dto.LoginRequestDTO;
import com.flexrate.flexrate_back.member.dto.LoginResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @Operation(
            summary = "로그인",
            description = "이메일/비밀번호 또는 패스키(FIDO2)를 이용한 로그인",
            tags = { "Auth Controller" }
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO loginRequestDTO) {
        LoginResponseDTO response = loginService.login(loginRequestDTO);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "로그아웃",
            description = "로그인된 사용자의 로그아웃을 처리하며, refreshToken을 삭제합니다.",
            tags = { "Auth Controller" }
    )
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam Long memberId) {
        loginService.logout(memberId);
        return ResponseEntity.ok("로그아웃");
    }

//    @Operation(
//            summary = "refreshToken 블랙리스트 처리",
//            description = "refreshToken을 블랙리스트에 추가하여 무효화합니다.",
//            tags = { "Auth Controller" }
//    )
//    @PostMapping("/blacklist")
//    public ResponseEntity<String> blacklistRefreshToken(@RequestParam Long memberId) {
//        loginService.blacklistRefreshToken(memberId);
//        return ResponseEntity.ok("refreshToken이 블랙리스트에 추가되었습니다.");
//    }
}