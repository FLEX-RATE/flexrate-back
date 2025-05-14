package com.flexrate.flexrate_back.member.api;

import com.flexrate.flexrate_back.member.application.EmailService;
import com.flexrate.flexrate_back.member.application.VerificationCodeService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final EmailService emailService;
    private final VerificationCodeService verificationCodeService;

    // 이메일로 인증 코드 전송
    @PostMapping("/send-verification-code")
    public ResponseEntity<String> sendVerificationCode(@RequestParam String email) {
        try {
            // 인증 코드 생성
            String verificationCode = emailService.generateVerificationCode();

            // 인증 코드 이메일로 전송
            emailService.sendVerificationEmail(email, verificationCode);

            // Redis에 인증 코드 저장
            verificationCodeService.storeVerificationCode(email, verificationCode);

            return ResponseEntity.ok("인증 코드가 이메일로 전송되었습니다.");
        } catch (MessagingException e) {
            return ResponseEntity.status(500).body("이메일 전송에 실패했습니다.");
        }
    }

    // 인증 코드 검증
    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@RequestParam String email, @RequestParam String code) {
        boolean isValid = verificationCodeService.validateVerificationCode(email, code);
        if (isValid) {
            return ResponseEntity.ok("인증 성공");
        } else {
            return ResponseEntity.status(400).body("인증 코드가 잘못되었습니다.");
        }
    }
}