package com.flexrate.flexrate_back.member.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.flexrate.flexrate_back.member.dto.*;
import com.upokecenter.cbor.CBORException;
import com.upokecenter.cbor.CBORObject;

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
import java.security.spec.*;
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
        log.info("passkeyDTO credentialKey: {}", passkeyDTO.credentialKey());
        log.info("passkeyDTO clientDataJSON: {}", passkeyDTO.clientDataJSON());
        log.info("passkeyDTO authenticatorData: {}", passkeyDTO.authenticatorData());
        log.info("passkeyDTO signature: {}", passkeyDTO.signature());
        log.info("passkeyDTO publicKey: {}", passkeyDTO.publicKey());
        log.info("passkeyDTO signCount: {}", passkeyDTO.signCount());


        log.info("패스키 등록 시도: memberId={}, credentialKey={}", member.getMemberId(), passkeyDTO.credentialKey());

        if (fidoCredentialRepository.existsByCredentialKey(passkeyDTO.credentialKey()) ||
                fidoCredentialRepository.existsByPublicKey(passkeyDTO.publicKey())) {
            throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED);
        }

        byte[] clientDataJSONBytes = Base64.getUrlDecoder().decode(passkeyDTO.clientDataJSON());
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

        var decode = Base64.getUrlDecoder().decode(passkeyDTO.signature());

        log.debug("signature decoded (length={}): {}", decode.length, Arrays.toString(decode));

        boolean isValid = verifySignature(
                passkeyDTO.publicKey(),
                Base64.getUrlDecoder().decode(passkeyDTO.authenticatorData()),
                clientDataHash,
                decode
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
            log.warn("request.credentialId() is null? {}", request.credentialId() == null);

            log.debug("=== [parseAndBuildDTO] 수신된 FIDO2 등록 요청 데이터 ===");
            log.debug("credentialId: {}", request.credentialId());
            log.debug("rawId: {}", request.rawId());
            log.debug("clientDataJSON: {}", request.clientDataJSON());
            log.debug("attestationObject: {}", request.attestationObject());
            log.debug("deviceInfo: {}", request.deviceInfo());
            log.debug("publicKey: {}", request.publicKey());
            log.debug("signCount: {}", request.signCount());
            log.debug("authenticatorData: {}", request.authenticatorData());
            log.debug("signature: {}", request.signature());


            log.debug("요청받은 데이터: {}", request);

            // URL-safe Base64를 일반 Base64로 변환
            String attestationObjectBase64Url = request.attestationObject();
            String attestationObjectBase64 = attestationObjectBase64Url.replace('_', '/').replace('-', '+');

            // Base64 변환 후 길이가 4의 배수가 되지 않으면 패딩 추가
            while (attestationObjectBase64.length() % 4 != 0) {
                attestationObjectBase64 += "=";
            }

            // 변환된 Base64 문자열 로그
            log.debug("변환된 Base64 attestationObject: {}", attestationObjectBase64);

            // Base64 디코딩
            byte[] attestationObjectBytes = Base64.getUrlDecoder().decode(attestationObjectBase64Url);
            log.debug("AttestationObject 바이트 길이: {}", attestationObjectBytes.length);

            byte[] authDataBytes = WebAuthnCborParser.extractAuthData(attestationObjectBytes);
            log.debug("authData 바이트 길이: {}", authDataBytes.length);

            // signCount 추출
            long signCount = WebAuthnCborParser.extractSignCount(authDataBytes);
            log.debug("signCount: {}", signCount);

            // 공개 키 추출
            byte[] publicKeyBytes = WebAuthnCborParser.extractCredentialPublicKeyBytes(authDataBytes);
            String publicKeyPem = convertPublicKeyToPem(publicKeyBytes);
            log.debug("공개 키 PEM: {}", publicKeyPem);

            // DTO 생성
            return PasskeyRequestDTO.builder()
                    .credentialKey(request.credentialId())
                    .clientDataJSON(request.clientDataJSON())
                    .publicKey(publicKeyPem)
                    .signCount(signCount)
                    .deviceInfo(request.deviceInfo())
                    .authenticatorData(request.authenticatorData())
                    .signature(request.signature())
                    .build();
        } catch (IllegalArgumentException e) {
            log.error("Base64 디코딩 오류 또는 잘못된 데이터 형식", e);
            throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED, e);
        } catch (CBORException e) {
            log.error("파싱 오류", e);
            throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED, e);
        } catch (Exception e) {
            log.error("parseAndBuildDTO 실패", e);
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
            // 1. PEM → PublicKey 객체로 변환
            String publicKeyPEM = pemPublicKey
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyPEM);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

            // 2. signedData = authenticatorData || clientDataHash
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(authenticatorData);
            baos.write(clientDataHash);
            byte[] signedData = baos.toByteArray();

            // 3. 서명(raw r||s)을 DER 인코딩 형식으로 변환
            byte[] derEncodedSignature = convertRawEcdsaSignatureToDer(signature);

            // 4. 서명 검증
            Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(signedData);

            return ecdsaVerify.verify(derEncodedSignature);

        } catch (Exception e) {
            log.error("서명 검증실패", e);
            throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED, e);
        }
    }

    // helper 메서드: raw (r||s) → ASN.1 DER
    private byte[] convertRawEcdsaSignatureToDer(byte[] rawSignature) {
        if (rawSignature.length != 64) {
            throw new IllegalArgumentException("Invalid raw ECDSA signature length");
        }

        byte[] rBytes = Arrays.copyOfRange(rawSignature, 0, 32);
        byte[] sBytes = Arrays.copyOfRange(rawSignature, 32, 64);

        BigInteger r = new BigInteger(1, rBytes);
        BigInteger s = new BigInteger(1, sBytes);

        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new ASN1Integer(r));
        v.add(new ASN1Integer(s));
        DERSequence seq = new DERSequence(v);

        try {
            return seq.getEncoded();
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode DER sequence", e);
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

    public String convertPublicKeyToPem(byte[] publicKeyBytes) throws Exception {
        try {
            // RSA도 아니면 COSE 포맷으로 간주
            CBORObject coseKey = CBORObject.DecodeFromBytes(publicKeyBytes);
            System.out.println("COSE Key 구조: " + coseKey.toString());


            // alg (3번 키) 검증: -7 (ES256)인 경우만 처리
            int alg = coseKey.get(CBORObject.FromObject(3)).AsInt32();
            if (alg != -7) {
                throw new IllegalArgumentException("지원하지 않는 알고리즘: " + alg);
            }
                // x y 값 좌표 추출
                byte[] x = coseKey.get(CBORObject.FromObject(-2)).GetByteString();
                byte[] y = coseKey.get(CBORObject.FromObject(-3)).GetByteString();

                // ECParameterSpec 가져오기 (NIST P-256)
                AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
                parameters.init(new ECGenParameterSpec("secp256r1"));
                ECParameterSpec ecSpec = parameters.getParameterSpec(ECParameterSpec.class);

                // ECPoint 및 공개키 생성
                ECPoint ecPoint = new ECPoint(new BigInteger(1, x), new BigInteger(1, y));
                ECPublicKeySpec ecPubSpec = new ECPublicKeySpec(ecPoint, ecSpec);
                KeyFactory kf = KeyFactory.getInstance("EC");
                PublicKey publicKey = kf.generatePublic(ecPubSpec);

                // 공개키를 PEM 변환
            return convertToPem(publicKey);
        } catch(Exception e2){
            System.out.println("COSE decode 실패. publicKeyBytes (hex): " + bytesToHex(publicKeyBytes));
            throw new IllegalArgumentException("지원하지 않는 공개키 포맷입니다.", e2);
        }
    }

    private String convertToPem(PublicKey publicKey) {
        String base64Encoded = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        StringBuilder pemBuilder = new StringBuilder();
        pemBuilder.append("-----BEGIN PUBLIC KEY-----\n");
        for (int i = 0; i < base64Encoded.length(); i += 64) {
            int end = Math.min(i + 64, base64Encoded.length());
            pemBuilder.append(base64Encoded, i, end).append("\n");
        }
        pemBuilder.append("-----END PUBLIC KEY-----");
        return pemBuilder.toString();
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }
}