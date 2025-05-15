package com.flexrate.flexrate_back.common.dto;

import lombok.Builder;

@Builder
public record EmailVerificationRequest(
        String code,
        String email
) {}
