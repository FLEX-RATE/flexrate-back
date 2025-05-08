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
}
