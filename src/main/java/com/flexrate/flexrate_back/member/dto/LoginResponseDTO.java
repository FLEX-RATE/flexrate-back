package com.flexrate.flexrate_back.member.dto;

import lombok.Builder;

@Builder
public record LoginResponseDTO(
        Long userId,
        String email,
        String accessToken,
        String refreshToken,
        String challenge
) {}