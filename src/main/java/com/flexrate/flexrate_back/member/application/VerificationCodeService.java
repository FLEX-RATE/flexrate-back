package com.flexrate.flexrate_back.member.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private final StringRedisTemplate redisTemplate;

    // 인증 코드 저장
    public void storeVerificationCode(String email, String verificationCode) {
        redisTemplate.opsForValue().set(email, verificationCode, 5, TimeUnit.MINUTES); // 5분 동안 유효
    }

    // 인증 코드 검증
    public boolean validateVerificationCode(String email, String inputCode) {
        String storedCode = redisTemplate.opsForValue().get(email);
        return storedCode != null && storedCode.equals(inputCode);
    }
}