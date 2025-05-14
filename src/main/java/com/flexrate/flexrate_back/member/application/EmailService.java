package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.common.exception.FlexrateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    // 인증 코드 생성
    public String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10)); // 0-9 랜덤 숫자 생성
        }
        return code.toString();
    }

    // 이메일 인증 코드 전송
    public void sendVerificationEmail(String recipientEmail, String verificationCode) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        // 인증 이메일의 발신자 이메일을 고정된 값이 아니라 입력받은 이메일로 설정
        helper.setFrom("your-email@gmail.com"); // 여기서 'your-email@gmail.com'은 발신자 이메일로 고정된 값입니다.
        helper.setTo(recipientEmail); // 사용자로부터 받은 이메일 주소를 수신자 이메일로 설정
        helper.setSubject("이메일 인증 코드");
        helper.setText("인증 코드: " + verificationCode);

        // 로그 추가 (로깅 예시)
        log.info("Sending verification code to {}", recipientEmail);

        try {
            mailSender.send(message);
            log.info("Verification email sent successfully to {}", recipientEmail);
        } catch (FlexrateException e) {
            log.error("Failed to send verification email to {}: {}", recipientEmail, e.getMessage());
            throw e;
        }
    }
}