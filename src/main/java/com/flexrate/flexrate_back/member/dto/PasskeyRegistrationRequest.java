package com.flexrate.flexrate_back.member.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record PasskeyRegistrationRequest(
        @JsonProperty("credentialId") String credentialId,
        @JsonProperty("rawId") String rawId,
        @JsonProperty("clientDataJSON") String clientDataJSON,
        @JsonProperty("attestationObject") String attestationObject,
        @JsonProperty("deviceInfo") String deviceInfo,
        @JsonProperty("publicKey") String publicKey,
        @JsonProperty("signCount") Long signCount,
        @JsonProperty("authenticatorData") String authenticatorData,
        @JsonProperty("signature") String signature
) {
    @JsonCreator
    public PasskeyRegistrationRequest {
        // compact constructor; 별도 코드 없이 자동 필드 초기화됨
    }
}

