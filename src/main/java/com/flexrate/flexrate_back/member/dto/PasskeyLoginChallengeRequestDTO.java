package com.flexrate.flexrate_back.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record PasskeyLoginChallengeRequestDTO(
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @NotBlank(message = "이메일은 필수 항목입니다.")
        String email
) {
    @Builder
    public PasskeyLoginChallengeRequestDTO {}
}
