package com.flexrate.flexrate_back.member.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.upokecenter.cbor.CBORObject;
import com.upokecenter.cbor.CBORType;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.flexrate.flexrate_back.member.dto.PasskeyRegistrationRequest;
import com.flexrate.flexrate_back.member.dto.PasskeyRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
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

    @Transactional
    public void registerPasskey(Member member, PasskeyRequestDTO passkeyDTO) {
        log.info("패스키 등록 시도: memberId={}, credentialKey={}", member.getMemberId(), passkeyDTO.credentialKey());

        if (fidoCredentialRepository.existsByCredentialKey(passkeyDTO.credentialKey()) ||
                fidoCredentialRepository.existsByPublicKey(passkeyDTO.publicKey())) {
            throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED);
        }

        boolean isValid = verifySignature(
                passkeyDTO.publicKey(),
                Base64.getDecoder().decode(passkeyDTO.authenticatorData()),
                Base64.getDecoder().decode(passkeyDTO.clientDataJSON()),
                Base64.getDecoder().decode(passkeyDTO.signature())
        );

        if (!isValid) {
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

    public Optional<FidoCredential> authenticatePasskey(Long userId, PasskeyAuthenticationDTO passkeyData, String challengeFromClient) {
        memberRepository.findById(userId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        String redisKey = CHALLENGE_KEY_PREFIX + userId;
        String savedChallenge = redisUtil.get(redisKey);

        if (savedChallenge == null) {
            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
        }

        try {
            String clientDataJsonStr = new String(Base64.getDecoder().decode(passkeyData.clientDataJSON()));
            ObjectMapper mapper = new ObjectMapper();
            JsonNode clientData = mapper.readTree(clientDataJsonStr);

            String challengeInClientData = clientData.get("challenge").asText();
            String decodedChallengeInClientData = new String(Base64.getUrlDecoder().decode(challengeInClientData));
            String decodedSavedChallenge = new String(Base64.getUrlDecoder().decode(savedChallenge));
            if (!decodedChallengeInClientData.equals(decodedSavedChallenge)) {
                throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
            }

            String origin = clientData.get("origin").asText();
            String expectedOrigin = "https://your.domain.com";
            if (!expectedOrigin.equals(origin)) {
                throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
            }

        } catch (Exception e) {
            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS, e);
        }

        redisUtil.delete(redisKey);

        FidoCredential credential = fidoCredentialRepository.findByMember_MemberId(userId).stream()
                .filter(FidoCredential::isActive)
                .findFirst()
                .orElseThrow(() -> new FlexrateException(ErrorCode.INVALID_CREDENTIALS));

        try {
            byte[] clientDataHash = MessageDigest.getInstance("SHA-256")
                    .digest(Base64.getDecoder().decode(passkeyData.clientDataJSON()));
            byte[] authenticatorData = Base64.getDecoder().decode(passkeyData.authenticatorData());
            byte[] signature = Base64.getDecoder().decode(passkeyData.signature());

            boolean isVerified = verifySignature(credential.getPublicKey(), authenticatorData, clientDataHash, signature);
            if (!isVerified) {
                throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
            }

            int signCountFromDevice = extractSignCount(authenticatorData);
            if (signCountFromDevice <= credential.getSignCount()) {
                throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
            }

            credential.updateSignCountAndLastUsed(signCountFromDevice, LocalDateTime.now());
            fidoCredentialRepository.save(credential);

        } catch (Exception e) {
            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS, e);
        }

        return Optional.of(credential);
    }

    public PasskeyRequestDTO parseAndBuildDTO(PasskeyRegistrationRequest request) {
        try {
            byte[] attestationObjectBytes = Base64.getDecoder().decode(request.attestationObject());

            CBORObject attestationObject = CBORObject.DecodeFromBytes(attestationObjectBytes);
            CBORObject authDataObject = attestationObject.get("authData");
            if (authDataObject == null || authDataObject.getType() != CBORType.ByteString) {
                throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED);

            }

            byte[] authDataBytes = authDataObject.GetByteString();
            int signCount = extractSignCount(authDataBytes);
            String publicKeyPem = extractPublicKey(authDataBytes);

            return PasskeyRequestDTO.builder()
                    .credentialKey(request.credentialKey())
                    .authenticatorData(request.authenticatorData())
                    .clientDataJSON(request.clientDataJSON())
                    .signature(request.signature())
                    .publicKey(publicKeyPem)
                    .signCount(signCount)
                    .deviceInfo(request.deviceInfo())
                    .build();

        } catch (Exception e) {
            log.error("parseAndBuildDTO 실패", e);
            throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED, e);
        }
    }

    private String extractPublicKey(byte[] authDataBytes) {
        try {
            int aaguidLength = 16;
            int credIdLengthIndex = 53;
            int credIdLength = ByteBuffer.wrap(authDataBytes, credIdLengthIndex, 2).getShort();

            int credIdStart = 55;
            int pubKeyStart = credIdStart + credIdLength;

            byte[] pubKeyBytes = new byte[authDataBytes.length - pubKeyStart];
            System.arraycopy(authDataBytes, pubKeyStart, pubKeyBytes, 0, pubKeyBytes.length);

            CBORObject coseKey = CBORObject.DecodeFromBytes(pubKeyBytes);

            byte[] x = coseKey.get(-2).GetByteString();
            byte[] y = coseKey.get(-3).GetByteString();

            byte[] uncompressed = new byte[65];
            uncompressed[0] = 0x04;
            System.arraycopy(x, 0, uncompressed, 1, 32);
            System.arraycopy(y, 0, uncompressed, 33, 32);

            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey ecPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(uncompressed));

            return "-----BEGIN PUBLIC KEY-----\n" +
                    Base64.getEncoder().encodeToString(ecPublicKey.getEncoded()) +
                    "\n-----END PUBLIC KEY-----";

        } catch (Exception e) {
            throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED, e);
        }
    }

    private int extractSignCount(byte[] authData) {
        if (authData.length < 37) {
            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
        }
        return ((authData[33] & 0xFF) << 24)
                | ((authData[34] & 0xFF) << 16)
                | ((authData[35] & 0xFF) << 8)
                | (authData[36] & 0xFF);
    }

    private boolean verifySignature(String pemPublicKey, byte[] authenticatorData, byte[] clientDataHash, byte[] signature) {
        try {
            String publicKeyPEM = pemPublicKey
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyPEM);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(authenticatorData);
            baos.write(clientDataHash);
            byte[] signedData = baos.toByteArray();

            Signature sig = Signature.getInstance("SHA256withECDSA");
            sig.initVerify(publicKey);
            sig.update(signedData);

            return sig.verify(signature);

        } catch (Exception e) {
            log.error("Signature verification failed", e);
            throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED, e);
        }
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

    private byte[] randomChallengeBytes() {
        byte[] challenge = new byte[32];
        new SecureRandom().nextBytes(challenge);
        return challenge;
    }


}