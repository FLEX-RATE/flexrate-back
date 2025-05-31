package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.auth.domain.FidoCredential;
import com.flexrate.flexrate_back.auth.domain.MfaLog;
import com.flexrate.flexrate_back.auth.enums.AuthResult;
import com.flexrate.flexrate_back.auth.enums.MfaType;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.common.util.StringRedisUtil;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.FidoCredentialRepository;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.domain.repository.MfaLogRepository;
import com.flexrate.flexrate_back.member.dto.PasskeyAuthenticationDTO;
import com.flexrate.flexrate_back.member.dto.PasskeyRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WebAuthnService {

    private final MemberRepository memberRepository;
    private final FidoCredentialRepository fidoCredentialRepository;
    private final MfaLogRepository mfaLogRepository;
    private final StringRedisUtil redisUtil;

    private static final String CHALLENGE_KEY_PREFIX = "fido:challenge:";
    private static final Duration CHALLENGE_TTL = Duration.ofMinutes(5);

    // 챌린지 생성 메소드 등록과 인증 공용
    public String generateChallenge(Long userId) {
        memberRepository.findById(userId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        // 테스트용 챌린지 5분
        String challenge = UUID.randomUUID().toString();
        String redisKey = CHALLENGE_KEY_PREFIX + userId;
        redisUtil.set(redisKey, challenge, CHALLENGE_TTL);
        return challenge;
    }

    // 패스키 등록
    @Transactional
    public void registerPasskey(Member member, PasskeyRequestDTO passkeyDTO) {
        log.debug("패스키 중복 체크: memberId={}", member.getMemberId());

        // 중복된 공개키와 Credential ID 체크
        if (fidoCredentialRepository.existsByPublicKey(passkeyDTO.publicKey()) ||
                fidoCredentialRepository.existsByCredentialId(passkeyDTO.credentialId())) {
            log.warn("중복된 패스키 등록 시도: memberId={}, credentialId={}", member.getMemberId(), passkeyDTO.credentialId());
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

        log.debug("패스키 등록 완료: memberId={}, credentialId={}", member.getMemberId(), passkeyDTO.credentialId());
    }

    // 패스키 인증
    public Optional<FidoCredential> authenticatePasskey(Long userId, PasskeyAuthenticationDTO passkeyData, String challengeFromClient) {
        memberRepository.findById(userId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        // Redis에서 저장된 챌린지 조회
        String redisKey = CHALLENGE_KEY_PREFIX + userId;
        String savedChallenge = redisUtil.get(redisKey);

        if (savedChallenge == null || !savedChallenge.equals(challengeFromClient)) {
            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
        }

        redisUtil.delete(redisKey); // 인증 후 삭제

        // FIDO 자격 증명 조회
        FidoCredential credential = fidoCredentialRepository.findByMember_MemberId(userId)
                .filter(FidoCredential::isActive)
                .orElseThrow(() -> new FlexrateException(ErrorCode.INVALID_CREDENTIALS));


        try {
            // 1. clientDataJSON → SHA-256 해시
            byte[] clientDataHash = MessageDigest.getInstance("SHA-256")
                    .digest(Base64.getDecoder().decode(passkeyData.clientDataJSON()));

            // 2. authenticatorData
            byte[] authenticatorData = Base64.getDecoder().decode(passkeyData.authenticatorData());

            // 3. signature (서명 값)
            byte[] signature = Base64.getDecoder().decode(passkeyData.signature());

            // 4. signedData = authenticatorData || clientDataHash
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(authenticatorData);
            baos.write(clientDataHash);
            byte[] signedData = baos.toByteArray();

            // 5. 공개키로 서명 검증
            boolean isVerified = verifySignature(credential.getPublicKey(), authenticatorData, clientDataHash, signature);

            if (!isVerified) {
                throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
            }
        } catch (Exception e) {
            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS, e);
        }

        return Optional.of(credential);
    }

    // 서명 검증 로직
    public boolean verifySignatureForRegistration(String pemPublicKey, String challenge, Long credentialId) {
        try {
            // credentialId를 String으로 변환한 후, Base64로 인코딩한 후 디코딩
            String credentialIdBase64 = Base64.getEncoder().encodeToString(credentialId.toString().getBytes());

            // 서명 검증
            return verifySignature(pemPublicKey, challenge.getBytes(), new byte[0], Base64.getDecoder().decode(credentialIdBase64));
        } catch (Exception e) {
            throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED, e);
        }
    }

    private MfaLog saveMfaLog(Member member, String deviceInfo, AuthResult result) {
        MfaLog mfaLog = MfaLog.builder()
                .mfaType(MfaType.FIDO2)
                .result(result)
                .authenticatedAt(LocalDateTime.now())
                .deviceInfo(deviceInfo)
                .build();
        return mfaLogRepository.save(mfaLog);
    }

    private boolean verifySignature(String pemPublicKey, byte[] authenticatorData, byte[] clientDataJSON, byte[] signature) throws FlexrateException {
        try {
            // 공개키 PEM에서 PublicKey 객체로 변환
            String publicKeyPEM = pemPublicKey
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "").strip();  // 공백 제거 및 strip

            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyPEM);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

            // clientDataJSON을 SHA-256 해시로 변환
            byte[] clientDataHash = MessageDigest.getInstance("SHA-256").digest(clientDataJSON);

            // authenticatorData와 clientDataHash 결합하여 서명 검증용 데이터 생성
            byte[] dataToVerify = concatenate(authenticatorData, clientDataHash);

            // ECDSA 서명 검증
            Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(dataToVerify);

            return ecdsaVerify.verify(signature);
        } catch (Exception e) {
            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS, e);
        }
    }


    // 데이터 결합 함수
    private byte[] concatenate(byte[] a, byte[] b) {
        byte[] combined = new byte[a.length + b.length];
        System.arraycopy(a, 0, combined, 0, a.length);
        System.arraycopy(b, 0, combined, a.length, b.length);
        return combined;
    }
}