package com.flexrate.flexrate_back.member.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Base64;

public class WebAuthnCborParser {

    private static final CBORFactory cborFactory = new CBORFactory();
    private static final ObjectMapper cborMapper = new ObjectMapper(cborFactory);

    public static JsonNode parseAttestationObject(byte[] attestationObjectBytes) throws IOException {
        return cborMapper.readTree(attestationObjectBytes);
    }

    // authData 추출
    public static byte[] extractAuthData(byte[] attestationObjectBytes) throws IOException {
        JsonNode rootNode = parseAttestationObject(attestationObjectBytes);
        JsonNode authDataNode = rootNode.get("authData");
        if (authDataNode == null || !authDataNode.isBinary()) {
            throw new IOException("authData is missing or not binary");
        }
        return authDataNode.binaryValue();
    }

    // signCount 추출 (authData 안 33~36byte)
    public static long extractSignCount(byte[] authData) {
        if (authData.length < 37) {
            throw new IllegalArgumentException("authData too short to contain signCount");
        }
        return ((authData[33] & 0xFFL) << 24)
                | ((authData[34] & 0xFFL) << 16)
                | ((authData[35] & 0xFFL) << 8)
                | (authData[36] & 0xFFL);
    }

    // publicKey 추출 (authData 내부의 CBOR 형식인 credentialPublicKey 파싱)
    // authData 포맷:
    // https://www.w3.org/TR/webauthn-2/#sec-authenticator-data
    // 37번째 바이트부터 credentialPublicKey가 CBOR로 시작
    public static byte[] extractCredentialPublicKeyBytes(byte[] authData) throws IOException {
        // authData 구조
        // [rpIdHash(32) | flags(1) | signCount(4) | attestedCredentialData ...]
        // attestedCredentialData 시작 = 37번째 바이트부터

        if (authData.length <= 37) {
            throw new IllegalArgumentException("authData too short for credentialPublicKey");
        }

        // attestedCredentialData layout:
        // aaguid(16 bytes) + credentialIdLength(2 bytes) + credentialId + credentialPublicKey(CBOR)

        int offset = 37; // start of attestedCredentialData

        // aaguid (16 bytes)
        offset += 16;

        // credentialIdLength (2 bytes, big endian)
        int credentialIdLength = ((authData[offset] & 0xFF) << 8) | (authData[offset + 1] & 0xFF);
        offset += 2;

        // credentialId (variable length)
        offset += credentialIdLength;

        // credentialPublicKey starts here
        int publicKeyStart = offset;

        // credentialPublicKey는 CBOR 형식인데, 크기는 가변적임.
        // 전체 authData 끝까지 credentialPublicKey라고 가정 가능
        int publicKeyLength = authData.length - publicKeyStart;

        byte[] publicKeyBytes = new byte[publicKeyLength];
        System.arraycopy(authData, publicKeyStart, publicKeyBytes, 0, publicKeyLength);

        return publicKeyBytes;
    }

    // credentialPublicKey를 PEM 형식의 공개키 문자열로 변환하는 메서드는 별도 구현 필요
}
