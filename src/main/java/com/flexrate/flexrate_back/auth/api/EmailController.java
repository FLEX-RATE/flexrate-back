package com.flexrate.flexrate_back.auth.api;

import com.flexrate.flexrate_back.auth.application.EmailService;
import com.flexrate.flexrate_back.auth.dto.EmailRequest;
import com.flexrate.flexrate_back.common.dto.EmailVerificationRequest;
import com.flexrate.flexrate_back.member.application.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/email")
public class EmailController {
    private final EmailService emailService;

    /**
     * 주어진 이메일로 인증 확인 메일을 보내는 메서드이다.
     * @param request 이메일이 포함된 DTO
     */
    @PostMapping("/send")
    @Operation(summary = "인증메일 발송", description = "인증 확인 메일을 보내는 API", responses = {
            @ApiResponse(responseCode = "200", description = "성공"),
    })
    public ResponseEntity<?> sendAuthEmail(@RequestBody EmailRequest request) {
        emailService.sendAuthEmail(request.email());
        return ResponseEntity.ok(null);
    }
    /**
     * 인증번호를 검증하고, 성공 시 이메일을 반환하는 메서드이다.
     * @param request 인증번호 검증 요청 DTO
     */
    @PostMapping("/verification")
    @Operation(summary = "인증번호 검증", description = "인증번호가 유효한지 검증하는 API", responses = {
            @ApiResponse(responseCode = "200", description = "성공"),
    })
    public ResponseEntity<?> verifyAuthCode(@RequestBody EmailVerificationRequest request) {
        emailService.verifyAuthCode(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}