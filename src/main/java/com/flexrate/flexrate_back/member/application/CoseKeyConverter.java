package com.flexrate.flexrate_back.member.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

public class CoseKeyConverter {

    private static final CBORFactory cborFactory = new CBORFactory();
    private static final ObjectMapper cborMapper = new ObjectMapper(cborFactory);

    static {
        // BouncyCastle 보안 프로바이더 등록 (중복 등록 문제 방지)
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * COSE_Key CBOR 바이트 배열을 Java PublicKey로 변환
     */
    public static PublicKey convertCoseKeyToPublicKey(byte[] coseKeyBytes) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        JsonNode coseKeyNode = cborMapper.readTree(coseKeyBytes);

        int kty = coseKeyNode.get("1").intValue();
        if (kty != 2) { // EC2 키 타입인지 확인
            throw new IllegalArgumentException("Unsupported key type, expected EC2 (2)");
        }

        int crv = coseKeyNode.get("-1").intValue(); // 곡선 타입
        if (crv != 1) { // P-256 만 지원
            throw new IllegalArgumentException("Unsupported curve, expected P-256 (1)");
        }

        byte[] x = coseKeyNode.get("-2").binaryValue();
        byte[] y = coseKeyNode.get("-3").binaryValue();

        // ECPoint 생성
        ECPoint ecPoint = new ECPoint(new java.math.BigInteger(1, x), new java.math.BigInteger(1, y));

        // P-256 곡선 파라미터
        AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
        parameters.init(new ECGenParameterSpec("secp256r1"));
        ECParameterSpec ecParameters = parameters.getParameterSpec(ECParameterSpec.class);

        // 공개키 스펙
        ECPublicKeySpec pubSpec = new ECPublicKeySpec(ecPoint, ecParameters);

        KeyFactory keyFactory = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
        return keyFactory.generatePublic(pubSpec);
    }

    /**
     * PublicKey를 PEM 포맷 문자열로 변환 (Base64 인코딩 포함)
     */
    public static String convertPublicKeyToPEM(PublicKey publicKey) {
        String base64Encoded = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        StringBuilder sb = new StringBuilder();
        sb.append("-----BEGIN PUBLIC KEY-----\n");
        int index = 0;
        while (index < base64Encoded.length()) {
            sb.append(base64Encoded, index, Math.min(index + 64, base64Encoded.length()));
            sb.append("\n");
            index += 64;
        }
        sb.append("-----END PUBLIC KEY-----\n");
        return sb.toString();
    }
}
