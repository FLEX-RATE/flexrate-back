package com.flexrate.flexrate_back.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PinLoginRequestDTO(
        String pin
) {}