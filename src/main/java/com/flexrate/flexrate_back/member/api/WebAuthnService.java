package com.flexrate.flexrate_back.member.api;

import com.flexrate.flexrate_back.auth.domain.FidoCredential;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.FidoCredentialRepository;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class WebAuthnService {

    private final MemberRepository memberRepository;
    private final FidoCredentialRepository fidoCredentialRepository;
    private final RedisTemplate<String, String> redisTemplate;

    // Challenge 생성 및 Redis 저장
    public String generateChallenge(Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        String challenge = UUID.randomUUID().toString();
        String redisKey = "fido:challenge:" + userId;
        redisTemplate.opsForValue().set(redisKey, challenge, 5, TimeUnit.MINUTES);

        return challenge;
    }

    public Optional<FidoCredential> authenticatePasskey(Long userId, String passkeyData, String challengeFromClient) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        // 1. Redis에서 challenge 가져오기
        String redisKey = "fido:challenge:" + userId;
        String savedChallenge = redisTemplate.opsForValue().get(redisKey);

        if (savedChallenge == null || !savedChallenge.equals(challengeFromClient)) {
            throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED);
        }

        // 2. 등록된 패스키 찾기
        FidoCredential credential = fidoCredentialRepository.findByMember_MemberId(userId)
                .filter(FidoCredential::isActive)
                .orElseThrow(() -> new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED));

        try {
            // 공개키 기반 서명 검증 수행
            boolean isVerified = verifySignature(
                    credential.getPublicKey(),  // PEM 형식의 공개키 문자열
                    savedChallenge.getBytes(),  // challenge
                    Base64.getDecoder().decode(passkeyData) // 서명 (Base64 디코딩)
            );

            if (!isVerified) {
                throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED);
            }
        } catch (Exception e) {
            throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED);
        }

        return Optional.of(credential);
    }

    // Java 내장 Signature API로 서명 검증 (ECDSA with SHA256)
    private boolean verifySignature(String pemPublicKey, byte[] data, byte[] signature) throws Exception {
        // PEM 포맷 제거 및 디코딩
        String publicKeyPEM = pemPublicKey
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

        Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA");
        ecdsaVerify.initVerify(publicKey);
        ecdsaVerify.update(data);

        return ecdsaVerify.verify(signature);
    }
}

