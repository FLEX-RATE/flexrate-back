package com.flexrate.flexrate_back.member.api;

import com.flexrate.flexrate_back.member.application.SignupService;
import com.flexrate.flexrate_back.member.dto.SignupPasskeyDTO;
import com.flexrate.flexrate_back.member.dto.SignupPasswordRequestDTO;
import com.flexrate.flexrate_back.member.dto.SignupResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/*
 * 회원가입 로그인 API 컨트롤러
 * @since 2025.04.28
 * @author 윤영찬
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class SignUpController {

    private final SignupService signupService;

    /*
     * 회원가입
     * @param signupDTO 데이터
     * @return 생성된 회원 ID, 이메일 (201 Created)
     * @throws FlexrateException 유효성 검사 또는 중복 등 오류 발생 시
     * @since 2025.04.28
     */
    @Operation(
            description = "사용자로부터 이메일, 비밀번호, 이름 등의 정보를 입력받아 회원을 등록합니다. " +
                    "이메일 중복 여부와 입력값의 유효성을 검사합니다.",
            tags = { "Auth Controller" }
    )
    @PostMapping("/signup/password")
    public SignupResponseDTO signupByPassword(@RequestBody @Valid SignupPasswordRequestDTO dto) {
        return signupService.registerByPassword(dto);
    }

    @Operation(
            description = """
                - FIDO2 Passkey(공개키 기반 인증)로 회원가입을 요청.
                - 이메일, 이름, 생년월일, 성별, 소비 성향 등의 기본 정보와 함께 Passkey 등록 정보를 받습니다.
                - 공개키 서명 검증합니다.
                - 서명이 유효하지 않거나 이메일이 중복된 경우 오류가 발생합니다.
                """,
            tags = { "Auth Controller" }
    )
    @PostMapping("/signup/passkey")
    public SignupResponseDTO signupByPasskey(@RequestBody @Valid SignupPasskeyDTO dto) {
        return signupService.registerByPasskey(dto);
    }
}
