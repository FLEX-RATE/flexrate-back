package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.auth.domain.FidoCredential;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.FidoCredentialRepository;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.dto.PasskeyRequestDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(WebAuthnService.class);  // 추가

    // 챌린지 생성 메소드 등록과 인증 공용
    public String generateChallenge(Long userId) {
        memberRepository.findById(userId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        // 테스트용 챌린지 10분
        String challenge = UUID.randomUUID().toString();
        String redisKey = "fido:challenge:" + userId;
        redisTemplate.opsForValue().set(redisKey, challenge, 10, TimeUnit.MINUTES);

        logger.info("Generated challenge for user {}: {}", userId, challenge);

        return challenge;
    }

    // 패스키 등록
    public void registerPasskey(Member member, PasskeyRequestDTO passkeyDTO) {
        // 중복된 공개키와 Credential ID 체크
        if (fidoCredentialRepository.existsByPublicKey(passkeyDTO.publicKey()) ||
                fidoCredentialRepository.existsByCredentialId(passkeyDTO.credentialId())) {
            throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED);
        }

        // 공개키와 초기 서명 카운트 등록
        FidoCredential fidoCredential = FidoCredential.builder()
                .member(member)
                .publicKey(passkeyDTO.publicKey())
                .credentialId(passkeyDTO.credentialId())
                .signCount(passkeyDTO.signCount())
                .deviceInfo(passkeyDTO.deviceInfo())
                .isActive(true)
                .lastUsedDate(java.time.LocalDateTime.now())
                .build();

        fidoCredentialRepository.save(fidoCredential);
    }

    // 패스키 인증
    public Optional<FidoCredential> authenticatePasskey(Long userId, String passkeyData, String challengeFromClient) {
        // 회원 정보 조회
        memberRepository.findById(userId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        // Redis에서 저장된 챌린지 조회
        String redisKey = "fido:challenge:" + userId;
        String savedChallenge = redisTemplate.opsForValue().get(redisKey);

        // 챌린지 유효성 검사
        if (savedChallenge == null || !savedChallenge.equals(challengeFromClient)) {
            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);  // 잘못된 챌린지
        }

        // 챌린지 유효하면 즉시 삭제
        redisTemplate.delete(redisKey);

        // 자격 증명 조회
        FidoCredential credential = fidoCredentialRepository.findByMember_MemberId(userId)
                .filter(FidoCredential::isActive)
                .orElseThrow(() -> new FlexrateException(ErrorCode.INVALID_CREDENTIALS)); // A002 사용

        try {
            // 서명 검증
            boolean isVerified = verifySignature(
                    credential.getPublicKey(),
                    savedChallenge.getBytes(),
                    Base64.getDecoder().decode(passkeyData)
            );

            if (!isVerified) {
                throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS); // A002 사용
            }
        } catch (Exception e) {
            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS, e); // A002 사용
        }

        return Optional.of(credential);
    }

    // 서명 검증 로직
    public boolean verifySignatureForRegistration(String pemPublicKey, String challenge, String credentialId) {
        try {
            // credentialId를 String으로 변환한 후, Base64로 인코딩한 후 디코딩
            String credentialIdBase64 = Base64.getEncoder().encodeToString(credentialId.toString().getBytes());

            // 서명 검증
            return verifySignature(pemPublicKey, challenge.getBytes(), Base64.getDecoder().decode(credentialIdBase64));
        } catch (Exception e) {
            throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED, e);
        }
    }

    private boolean verifySignature(String pemPublicKey, byte[] data, byte[] signature) throws Exception {
        String publicKeyPEM = pemPublicKey
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "")  // 모든 개행 제거
                .strip();

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
