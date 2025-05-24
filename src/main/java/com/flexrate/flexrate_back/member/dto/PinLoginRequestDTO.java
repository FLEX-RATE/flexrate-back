package com.flexrate.flexrate_back.member.dto;

public record PinLoginRequestDTO(
        Long userId,
        String pin
) {}
