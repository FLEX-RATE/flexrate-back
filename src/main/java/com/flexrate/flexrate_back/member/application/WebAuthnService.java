package com.flexrate.flexrate_back.member.application;




import com.fasterxml.jackson.databind.JsonNode;
import com.flexrate.flexrate_back.member.dto.*;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequence;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.math.BigInteger;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

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

        byte[] clientDataJSONBytes = Base64.getDecoder().decode(passkeyDTO.clientDataJSON());
        log.debug("clientDataJSON (decoded bytes): {}", Arrays.toString(clientDataJSONBytes));

        byte[] clientDataHash = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            clientDataHash = digest.digest(clientDataJSONBytes);
            log.debug("clientDataJSON SHA-256 해시: {}", Arrays.toString(clientDataHash));
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 해시 생성 실패", e);
            throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED, e);
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
            String clientDataJsonStr = new String(Base64.getUrlDecoder().decode(passkeyData.clientDataJSON()));

            System.out.println("clientDataJSON: " + passkeyData.clientDataJSON());
            System.out.println("clientDataJsonStr: " + clientDataJsonStr);
            System.out.println("savedChallenge: " + savedChallenge);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode clientData = mapper.readTree(clientDataJsonStr);

            String challengeInClientData = clientData.get("challenge").asText();
            String decodedChallengeInClientData = new String(Base64.getUrlDecoder().decode(challengeInClientData));
            String decodedSavedChallenge = new String(Base64.getUrlDecoder().decode(savedChallenge));

            System.out.println("decodedChallengeInClientData: " + decodedChallengeInClientData);
            System.out.println("decodedSavedChallenge: " + decodedSavedChallenge);

            if (!decodedChallengeInClientData.equals(decodedSavedChallenge)) {
                throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
            }

            String origin = clientData.get("origin").asText();
            String expectedOrigin = "http://localhost:3000";
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
            log.debug("attestationObject (base64): {}", request.attestationObject());

            byte[] attestationObjectBytes = Base64.getDecoder().decode(request.attestationObject());
            log.debug("attestationObjectBytes 길이: {}", attestationObjectBytes.length);

            CBORObject attestationObject = CBORObject.DecodeFromBytes(attestationObjectBytes);
            log.debug("attestationObject: {}", attestationObject.toString());

            CBORObject authDataObject = attestationObject.get("authData");
            if (authDataObject == null) {
                log.error("authDataObject가 null입니다.");
                throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED);
            }

            if (authDataObject.getType() != CBORType.ByteString) {
                log.error("authDataObject 타입이 ByteString이 아닙니다: {}", authDataObject.getType());
                throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED);
            }

            byte[] authDataBytes = authDataObject.GetByteString();
            log.debug("authDataBytes 길이: {}", authDataBytes.length);

            int signCount = extractSignCount(authDataBytes);
            log.debug("signCount 추출값: {}", signCount);

            String publicKeyPem = extractPublicKey(authDataBytes);
            log.debug("publicKeyPem: {}", publicKeyPem);

            return PasskeyRequestDTO.builder()
                    .credentialKey(request.credentialId())
                    .clientDataJSON(request.clientDataJSON())
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
            int index = 0;

            // rpIdHash (32 bytes)
            index += 32;

            // flags (1 byte)
            index += 1;

            // signCount (4 bytes)
            index += 4;

            // aaguid (16 bytes)
            index += 16;

            // credentialIdLength (2 bytes, unsigned big endian)
            int credentialIdLength = ((authDataBytes[index] & 0xFF) << 8) | (authDataBytes[index + 1] & 0xFF);
            index += 2;

            // credentialId
            index += credentialIdLength;

            // credentialPublicKey (CBOR)
            byte[] credentialPublicKeyBytes = Arrays.copyOfRange(authDataBytes, index, authDataBytes.length);

            CBORObject publicKeyObject = CBORObject.DecodeFromBytes(credentialPublicKeyBytes);

            // 이후 필요한 대로 publicKeyObject 파싱
            return publicKeyObject.toString(); // 또는 PEM 변환 로직

        } catch (Exception e) {
            log.error("extractPublicKey 실패", e);
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

            byte[] derSignature = convertRawSignatureToDER(signature);

            Signature sig = Signature.getInstance("SHA256withECDSA");
            sig.initVerify(publicKey);
            sig.update(signedData);

            return sig.verify(derSignature);

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

    public Fido2RegisterOptionsResponse generateRegistrationOptions(Member member) {
        String challenge = generateChallenge(member.getMemberId()); // Redis에 저장됨

        // userId 숫자를 base64url 인코딩된 문자열로 변환
        String encodedUserId = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(BigInteger.valueOf(member.getMemberId()).toByteArray());

        List<Fido2RegisterOptionsResponse.PubKeyCredParam> pubKeyCredParams = List.of(
                new Fido2RegisterOptionsResponse.PubKeyCredParam("public-key", -7),     // ES256
                new Fido2RegisterOptionsResponse.PubKeyCredParam("public-key", -257)    // RS256
        );

        return new Fido2RegisterOptionsResponse(
                challenge,
                new Fido2RegisterOptionsResponse.UserDto(
                        encodedUserId,
                        member.getEmail(),
                        member.getName()
                ),
                new Fido2RegisterOptionsResponse.RpDto("localhost", "Flexrate"),
                pubKeyCredParams,
                60000L,
                "none",
                Map.of() // 확장 사용 안 함
        );
    }

    private byte[] convertRawSignatureToDER(byte[] rawSignature) throws IOException {
        if (rawSignature.length != 64) {
            throw new IllegalArgumentException("Invalid raw signature length");
        }

        byte[] rBytes = Arrays.copyOfRange(rawSignature, 0, 32);
        byte[] sBytes = Arrays.copyOfRange(rawSignature, 32, 64);

        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new ASN1Integer(new BigInteger(1, rBytes)));
        v.add(new ASN1Integer(new BigInteger(1, sBytes)));

        DERSequence sequence = new DERSequence(v);
        return sequence.getEncoded("DER");
    }
}