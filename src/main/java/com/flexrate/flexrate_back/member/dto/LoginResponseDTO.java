package com.flexrate.flexrate_back.member.dto;

import com.flexrate.flexrate_back.member.enums.Role;
import lombok.Builder;

@Builder
public record LoginResponseDTO(
        Long userId,
        String username,
        String email,
        String accessToken,
        String refreshToken,
        Role role,
        String challenge
) {}