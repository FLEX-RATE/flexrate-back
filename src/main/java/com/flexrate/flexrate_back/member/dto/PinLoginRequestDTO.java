package com.flexrate.flexrate_back.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PinLoginRequestDTO(
        @JsonProperty("memberId") Long userId,
        String pin
) {}