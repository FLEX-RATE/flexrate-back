package com.flexrate.flexrate_back.member.dto;

import lombok.Builder;

@Builder
public record PasskeyRequestDTO(
        // 패스키 식별자 (credentialKey가 FIDO2 인증기에서 전달하는 유니크 키)
        String credentialKey,
        // 공개키 (PEM 또는 Base64 인코딩된 공개키 문자열)
        String publicKey,
        // 서명 카운트 (재사용 방지용)
        long signCount,
        // 디바이스 정보 (브라우저/OS 등 사용자 환경 정보)
        String deviceInfo,
        // FIDO2 인증기에서 보내는 바이너리 데이터 (Base64 인코딩)
        String authenticatorData,
        // 클라이언트에서 보낸 JSON 데이터 (Base64 인코딩)
        String clientDataJSON,
        // 서명 값 (Base64 인코딩)
        String signature
) {
        @Builder
        public PasskeyRequestDTO {}
}
