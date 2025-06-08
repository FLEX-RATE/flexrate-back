package com.flexrate.flexrate_back.member.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record PasskeyRequestDTO(
        @JsonProperty("credentialKey") String credentialKey,
        @JsonProperty("clientDataJSON") String clientDataJSON,
        @JsonProperty("publicKey") String publicKey,
        @JsonProperty("signCount") Long signCount,
        @JsonProperty("deviceInfo") String deviceInfo,
        @JsonProperty("authenticatorData") String authenticatorData,
        @JsonProperty("signature") String signature
) {
    @JsonCreator
    public PasskeyRequestDTO {
        // 여기서 추가 검증이나 가공 필요 시 구현 가능
    }


}
