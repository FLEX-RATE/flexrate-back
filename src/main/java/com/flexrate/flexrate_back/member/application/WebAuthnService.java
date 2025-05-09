package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.auth.domain.FidoCredential;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.FidoCredentialRepository;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
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

    // 챌린지 생성 메소드
    public String generateChallenge(Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        // 챌린지 생성
        String challenge = UUID.randomUUID().toString();
        String redisKey = "fido:challenge:" + userId;
        redisTemplate.opsForValue().set(redisKey, challenge, 5, TimeUnit.MINUTES);

        return challenge;
    }

    // 패스키 인증 메소드
    public Optional<FidoCredential> authenticatePasskey(Long userId, String passkeyData, String challengeFromClient) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        // Redis에서 저장된 챌린지 조회
        String redisKey = "fido:challenge:" + userId;
        String savedChallenge = redisTemplate.opsForValue().get(redisKey);

        // 챌린지 유효성 검사
        if (savedChallenge == null || !savedChallenge.equals(challengeFromClient)) {
            throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED);
        }

        // 패스키 인증을 위한 자격 증명 조회
        FidoCredential credential = fidoCredentialRepository.findByMember_MemberId(userId)
                .filter(FidoCredential::isActive)
                .orElseThrow(() -> new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED));

        try {
            // 서명 검증
            boolean isVerified = verifySignature(
                    credential.getPublicKey(),
                    savedChallenge.getBytes(),
                    Base64.getDecoder().decode(passkeyData)
            );

            if (!isVerified) {
                throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED);
            }
        } catch (Exception e) {
            throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED);
        }

        return Optional.of(credential);
    }

    // 서명 검증 로직
    private boolean verifySignature(String pemPublicKey, byte[] data, byte[] signature) throws Exception {
        // PEM 형식의 공개키 처리
        String publicKeyPEM = pemPublicKey
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        // 공개키 생성
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

        // 서명 검증
        Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA");
        ecdsaVerify.initVerify(publicKey);
        ecdsaVerify.update(data);

        return ecdsaVerify.verify(signature);
    }
}
