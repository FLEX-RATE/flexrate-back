package com.flexrate.flexrate_back.member.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebAuthnCborParser {

    private static final CBORFactory cborFactory = new CBORFactory();
    private static final ObjectMapper cborMapper = new ObjectMapper(cborFactory);

    // AttestationObject 파싱
    public static JsonNode parseAttestationObject(byte[] attestationObjectBytes) throws IOException {
        log.debug("Received Attestation Object (base64 encoded): {}", Base64.getEncoder().encodeToString(attestationObjectBytes)); // 입력 값 로깅
        JsonNode rootNode = cborMapper.readTree(attestationObjectBytes);
        log.debug("Parsed Attestation Object: {}", rootNode); // 파싱된 결과 로그 출력
        if (!rootNode.has("authData")) {
            log.error("authData 필드가 존재하지 않음: {}", rootNode.toPrettyString());
            throw new IOException("Missing 'authData' field in attestationObject");
        }
        return rootNode;
    }

    // authData 추출
    public static byte[] extractAuthData(byte[] attestationObjectBytes) throws IOException {
        log.debug("Starting to extract authData from attestationObjectBytes (length: {})", attestationObjectBytes.length);
        JsonNode rootNode = parseAttestationObject(attestationObjectBytes);
        JsonNode authDataNode = rootNode.get("authData");

        if (authDataNode == null || !authDataNode.isBinary()) {
            log.error("authData is missing or not binary");
            throw new IOException("authData is missing or not binary");
        }

        byte[] authData = authDataNode.binaryValue();
        log.debug("Extracted authData (length: {}): {}", authData.length, Base64.getEncoder().encodeToString(authData)); // authData 로깅
        return authData;
    }

    // signCount 추출 (authData 안 33~36byte)
    public static long extractSignCount(byte[] authData) {
        log.debug("Starting to extract signCount from authData (length: {})", authData.length);
        if (authData.length < 37) {
            log.error("authData too short to contain signCount: expected at least 37 bytes, but got {}", authData.length);
            throw new IllegalArgumentException("authData too short to contain signCount");
        }

        long signCount = ((authData[33] & 0xFFL) << 24)
                | ((authData[34] & 0xFFL) << 16)
                | ((authData[35] & 0xFFL) << 8)
                | (authData[36] & 0xFFL);

        log.debug("Extracted signCount: {}", signCount);  // signCount 로깅
        return signCount;
    }

    // publicKey 추출 (authData 내부의 CBOR 형식인 credentialPublicKey 파싱)
    public static byte[] extractCredentialPublicKeyBytes(byte[] authData) throws IOException {
        log.debug("Starting to extract credentialPublicKeyBytes from authData (length: {})", authData.length);

        if (authData.length <= 37) {
            log.error("authData too short for credentialPublicKey: expected more than 37 bytes, but got {}", authData.length);
            throw new IllegalArgumentException("authData too short for credentialPublicKey");
        }

        int offset = 37; // 시작 위치
        offset += 16; // aaguid (16 bytes)
        int credentialIdLength = ((authData[offset] & 0xFF) << 8) | (authData[offset + 1] & 0xFF);
        offset += 2; // credentialIdLength (2 bytes)

        log.debug("Credential ID length: {}", credentialIdLength);  // credentialIdLength 로깅

        offset += credentialIdLength; // credentialId (variable length)
        int publicKeyStart = offset; // publicKey 시작 위치
        int publicKeyLength = authData.length - publicKeyStart;

        log.debug("Public key starts at offset: {}, length: {}", publicKeyStart, publicKeyLength); // publicKey 위치 로깅

        byte[] publicKeyBytes = new byte[publicKeyLength];
        System.arraycopy(authData, publicKeyStart, publicKeyBytes, 0, publicKeyLength);

        log.debug("Extracted credentialPublicKeyBytes (length: {}): {}", publicKeyBytes.length, Base64.getEncoder().encodeToString(publicKeyBytes));  // publicKeyBytes 로깅
        return publicKeyBytes;
    }
}
