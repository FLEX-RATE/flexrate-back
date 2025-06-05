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
import com.flexrate.flexrate_back.member.dto.PasskeyLoginChallengeResponseDTO;
import com.flexrate.flexrate_back.member.dto.PasskeyRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
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

        byte[] challengeBytes = new byte[32];
        new SecureRandom().nextBytes(challengeBytes);
        String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(challengeBytes);

        redisUtil.set(CHALLENGE_KEY_PREFIX + userId, challenge, CHALLENGE_TTL);

        log.debug("생성된 FIDO2 challenge for userId={}: {}", userId, challenge);
        return challenge;
    }

    // 패스키 등록
    @Transactional
    public void registerPasskey(Member member, PasskeyRequestDTO passkeyDTO) {
        log.info("패스키 등록 시도: memberId={}, credentialKey={}", member.getMemberId(), passkeyDTO.credentialKey());

        if (fidoCredentialRepository.existsByCredentialKey(passkeyDTO.credentialKey())) {
            log.warn("중복된 credentialKey: {}", passkeyDTO.credentialKey());
            throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED);
        }

        if (fidoCredentialRepository.existsByPublicKey(passkeyDTO.publicKey())) {
            log.warn("중복된 publicKey: {}", passkeyDTO.publicKey());
            throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED);
        }

        boolean isValid = verifySignature(
                passkeyDTO.publicKey(),
                Base64.getDecoder().decode(passkeyDTO.authenticatorData()),
                Base64.getDecoder().decode(passkeyDTO.clientDataJSON()),
                Base64.getDecoder().decode(passkeyDTO.signature())
        );

        if (!isValid) {
            log.warn("패스키 등록 실패 - 서명 검증 실패: memberId={}", member.getMemberId());
            throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED);
        }

        FidoCredential credential = FidoCredential.builder()
                .credentialKey(passkeyDTO.credentialKey())
                .member(member)
                .publicKey(passkeyDTO.publicKey())
                .signCount(passkeyDTO.signCount())
                .deviceInfo(passkeyDTO.deviceInfo())
                .isActive(true)
                .lastUsedDate(LocalDateTime.now())
                .build();

        fidoCredentialRepository.save(credential);
        log.info("패스키 등록 완료: credentialKey={}", passkeyDTO.credentialKey());
    }

    // 패스키 인증
    public Optional<FidoCredential> authenticatePasskey(Long userId, PasskeyAuthenticationDTO passkeyData, String challengeFromClient) {
        memberRepository.findById(userId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        String redisKey = CHALLENGE_KEY_PREFIX + userId;
        String savedChallenge = redisUtil.get(redisKey);

        if (savedChallenge == null || !savedChallenge.equals(challengeFromClient)) {
            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
        }

        redisUtil.delete(redisKey); // 인증 후 삭제

        FidoCredential credential = fidoCredentialRepository.findByMember_MemberId(userId).stream()
                .filter(FidoCredential::isActive)
                .findFirst()
                .orElseThrow(() -> new FlexrateException(ErrorCode.INVALID_CREDENTIALS));

        try {
            byte[] clientDataHash = MessageDigest.getInstance("SHA-256")
                    .digest(Base64.getDecoder().decode(passkeyData.clientDataJSON()));

            byte[] authenticatorData = Base64.getDecoder().decode(passkeyData.authenticatorData());
            byte[] signature = Base64.getDecoder().decode(passkeyData.signature());

            // signedData = authenticatorData || clientDataHash
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(authenticatorData);
            baos.write(clientDataHash);
            byte[] signedData = baos.toByteArray();

            // 서명 검증
            boolean isVerified = verifySignature(credential.getPublicKey(), authenticatorData, clientDataHash, signature);

            if (!isVerified) {
                throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
            }

            // authenticatorData에서 signCount 추출 (authenticatorData[33~36], 4 bytes)
            int signCountFromDevice = extractSignCount(authenticatorData);

            // 저장된 값보다 크지 않으면 해커로 부터 재사용 공격 의심
            if (signCountFromDevice <= credential.getSignCount()) {
                log.warn("Replay 공격 의심: memberId={}, credentialId={}, oldSignCount={}, newSignCount={}",
                        userId, credential.getCredentialId(), credential.getSignCount(), signCountFromDevice);
                throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
            }

            // 인증 성공 후 signCount, lastUsedDate 업데이트
            credential.updateSignCountAndLastUsed(signCountFromDevice, LocalDateTime.now());
            fidoCredentialRepository.save(credential);

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

    // 이메일 기준으로 challenge 값을 Redis에 저장
    public void saveChallengeForEmail(String email, String challenge) {
        String redisKey = "fido:challenge:email:" + email;
        redisUtil.set(redisKey, challenge, Duration.ofMinutes(5)); // 5분 TTL 예시
    }

    public PasskeyLoginChallengeResponseDTO generateLoginChallenge(Member member) {
        final String rpId = "localhost";
        String challenge = Base64.getEncoder().encodeToString(randomChallengeBytes());

        redisUtil.set("fido:challenge:login:" + member.getMemberId(), challenge, Duration.ofMinutes(5));

        List<String> credentialIds = fidoCredentialRepository.findByMember_MemberId(member.getMemberId()).stream()
                .map(fido -> String.valueOf(fido.getCredentialId()))
                .toList();

        return new PasskeyLoginChallengeResponseDTO(
                challenge,
                rpId,
                member.getMemberId().toString(),
                credentialIds
        );
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

    private boolean verifySignature(String pemPublicKey, byte[] authenticatorData, byte[] clientDataHash, byte[] signature) throws FlexrateException {
        try {
            // PEM 공개키 정제 및 디코딩
            String publicKeyPEM = pemPublicKey
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "").strip();

            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyPEM);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

            // 서명 검증용 데이터 생성 (authenticatorData || clientDataHash)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(authenticatorData);
            baos.write(clientDataHash);
            byte[] signedData = baos.toByteArray();

            // ECDSA with SHA-256 서명 검증 객체 생성
            Signature sig = Signature.getInstance("SHA256withECDSA");
            sig.initVerify(publicKey);
            sig.update(signedData);

            // 서명 검증 수행
            return sig.verify(signature);

        } catch (Exception e) {
            log.error("Signature verification failed", e);
            throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED, e);
        }
    }

    private byte[] randomChallengeBytes() {
        byte[] challenge = new byte[32];  // 32바이트 길이 (256비트)
        new SecureRandom().nextBytes(challenge);
        return challenge;
    }

    private int extractSignCount(byte[] authenticatorData) {
        if (authenticatorData.length < 37) {
            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
        }

        // signCount는 33~36 byte
        int signCount = ((authenticatorData[33] & 0xFF) << 24)
                | ((authenticatorData[34] & 0xFF) << 16)
                | ((authenticatorData[35] & 0xFF) << 8)
                | (authenticatorData[36] & 0xFF);
        return signCount;
    }

    // 데이터 결합 함수
    private byte[] concatenate(byte[] a, byte[] b) {
        byte[] combined = new byte[a.length + b.length];
        System.arraycopy(a, 0, combined, 0, a.length);
        System.arraycopy(b, 0, combined, a.length, b.length);
        return combined;
    }


}