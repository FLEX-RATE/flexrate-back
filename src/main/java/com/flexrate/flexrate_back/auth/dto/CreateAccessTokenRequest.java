package com.flexrate.flexrate_back.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateAccessTokenRequest {
    private String refreshToken;
}
