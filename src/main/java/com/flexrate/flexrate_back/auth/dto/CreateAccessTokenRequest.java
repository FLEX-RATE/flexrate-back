package com.flexrate.flexrate_back.auth.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateAccessTokenRequest {
    private String refreshToken;
}
