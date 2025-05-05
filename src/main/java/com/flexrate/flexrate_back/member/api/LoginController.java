package com.flexrate.flexrate_back.member.api;

import com.flexrate.flexrate_back.auth.domain.jwt.JwtTokenProvider;
import com.flexrate.flexrate_back.member.application.LoginService;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.dto.LoginRequestDTO;
import com.flexrate.flexrate_back.member.dto.LoginResponseDTO;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/*
 * 이메일과 비밀번호로 로그인
 * @param loginRequestDTO 로그인 요청 데이터
 * @return 로그인 성공 시 JWT 토큰과 사용자 정보
 * @throws FlexrateException 유효성 검사 또는 인증 오류 시
 * @since 2025.05.05
 */


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;
    private final JwtTokenProvider jwtTokenProvider;
    private final WebAuthnService webAuthnService;

    @Operation(
            summary = "이메일/비밀번호 로그인",
            description = "이메일과 비밀번호로 로그인을 진행하고, 성공 시 JWT 토큰을 발급합니다.",
            tags = { "Auth Controller" }
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO loginRequestDTO) {

        LoginResponseDTO responseDTO = loginService.loginWithEmailAndPassword(loginRequestDTO);

        // 패스키 인증이 필요한 경우
        if (responseDTO.requirePasskeyAuth()) {
            // 패스키 인증을 진행 (비즈니스 로직에 맞게 인증을 추가)
            boolean isAuthenticated = webAuthnService.authenticatePasskey(loginRequestDTO.userId(), loginRequestDTO.passkeyData());

            if (!isAuthenticated) {
                throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED); // 패스키 인증 실패 시 에러 처리
            }
        }

        // Member 객체를 얻는 로직 추가
        Member member = loginService.getMemberByEmail(responseDTO.email());

        // JWT 발급
        String accessToken = jwtTokenProvider.generateToken(member, Duration.ofMinutes(30));
        String refreshToken = jwtTokenProvider.generateToken(member, Duration.ofDays(7));

        return ResponseEntity.ok(LoginResponseDTO.builder()
                .memberId(member.getMemberId())
                .email(responseDTO.email())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .requirePasskeyAuth(false)
                .registeredPasskeys(responseDTO.registeredPasskeys())
                .build());
    }
}
