package com.flexrate.flexrate_back.member.dto;

import lombok.Builder;
import java.util.List;

@Builder
public record PasskeyLoginChallengeResponseDTO(
        String challenge,
        String rpId,
        String userHandle,
        List<String> allowedCredentialIds
) {
    @Builder
    public PasskeyLoginChallengeResponseDTO {}
}
