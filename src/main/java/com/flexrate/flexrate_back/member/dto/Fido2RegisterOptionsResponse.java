package com.flexrate.flexrate_back.member.dto;

import java.util.List;
import java.util.Map;

public record Fido2RegisterOptionsResponse(
        String challenge,
        UserDto user,
        RpDto rp,
        List<PubKeyCredParam> pubKeyCredParams,
        Long timeout,
        String attestation,
        Map<String, Object> extensions
) {
    public record UserDto(String id, String name, String displayName) {}
    public record RpDto(String id, String name) {}
    public record PubKeyCredParam(String type, int alg) {}
}
