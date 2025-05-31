package com.flexrate.flexrate_back.auth.api;

import com.flexrate.flexrate_back.auth.application.AuthService;
import com.flexrate.flexrate_back.auth.dto.PinRequest;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.application.MemberService;
import com.flexrate.flexrate_back.member.domain.Member;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * PIN 인증 API 컨트롤러
 *
 * @since 2025.05.31
 * @author 권민지
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final MemberService memberService;
    private final AuthService authService;

    @Operation(
            summary = "사용자 PIN 보유 여부 조회",
            description = "로그인된 사용자의 PIN 보유 여부를 조회합니다."
    )
    @GetMapping("/pin/registered")
    public ResponseEntity<Boolean> checkPinRegistered(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new FlexrateException(ErrorCode.UNAUTHORIZED);
        }

        Member member = memberService.findById(Long.parseLong(principal.getName()));
        return ResponseEntity.ok(authService.checkPinRegistered(member));
    }

    @Operation(
            summary = "입력 PIN 인증",
            description = "로그인된 사용자가 입력한 PIN이 등록된 PIN과 일치하는지 확인합니다."
    )
    @PostMapping("/pin/verify")
    public ResponseEntity<Boolean> verifyPin(@RequestBody PinRequest pinRequest, Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new FlexrateException(ErrorCode.UNAUTHORIZED);
        }

        Member member = memberService.findById(Long.parseLong(principal.getName()));
        return ResponseEntity.ok(authService.verifyPin(pinRequest, member));
    }
}
