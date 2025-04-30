package com.flexrate.flexrate_back.member.dto;

import lombok.Builder;
import lombok.Getter;

/*
 * 회원가입 성공 시 반환하는 DTO
 * @since 2025.04.28
 * @author 윤영찬
 */
@Getter
@Builder
public class SignupResponseDTO {
    private Long userId;
    private String email;
}
