package com.flexrate.flexrate_back.member.dto;

import lombok.Builder;

@Builder
public record LoginResponseDTO(
        Long userId,
        String username,
        String email,
        String accessToken,
        String challenge
) {}