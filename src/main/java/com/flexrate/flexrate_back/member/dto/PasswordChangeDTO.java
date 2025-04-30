package com.flexrate.flexrate_back.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


/*
 * 비밀번호 변경 요청 시 필요한 DTO.
 * @since 2025.04.29
 * @author 윤영찬
 */

@Getter
@Setter
public class PasswordChangeDTO {

    @Email(message = "유효한 이메일 형식을 입력해주세요.")
    @NotBlank(message = "이메일은 필수입니다.")
    private String email;

    @NotBlank(message = "현재 비밀번호는 필수입니다.")
    private String currentPassword;

    @NotBlank(message = "새 비밀번호는 필수입니다.")
    private String newPassword;
}
