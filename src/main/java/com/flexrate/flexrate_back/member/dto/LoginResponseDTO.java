package com.flexrate.flexrate_back.member.dto;

import java.util.List;

public record LoginResponseDTO(
        String accessToken,
        String refreshToken,
        String userName,
        List<String> passkeyList
) {}