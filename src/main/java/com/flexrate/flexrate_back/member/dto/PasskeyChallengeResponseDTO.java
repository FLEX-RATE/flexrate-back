package com.flexrate.flexrate_back.member.dto;

import java.util.List;

public record PasskeyChallengeResponseDTO(
        String challenge,
        String relyingPartyId,
        String relyingPartyName,
        String userId,
        String userName,
        String userDisplayName,
        Long timeout,
        List<String> pubKeyCredParams,
        String userVerification,
        Boolean residentKey
) {}
