package com.flexrate.flexrate_back.member.dto;

import lombok.Builder;

/*
 * 회원가입 성공 시 반환하는 DTO
 * @since 2025.04.28
 * @author 윤영찬
 */
@Builder
public record SignupResponseDTO (
        Long userId,
        String email
) {}