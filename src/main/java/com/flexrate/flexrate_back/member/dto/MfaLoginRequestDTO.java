package com.flexrate.flexrate_back.member.dto;


public record MfaLoginRequestDTO(
        Long memberId,
        String challenge,
        String clientDataJSON,
        String authenticatorData,
        String signature
) {}
