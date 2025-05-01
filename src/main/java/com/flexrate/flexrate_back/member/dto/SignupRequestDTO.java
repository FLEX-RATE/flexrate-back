package com.flexrate.flexrate_back.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.flexrate.flexrate_back.member.enums.ConsumptionType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

/*
 * 회원가입 요청 시 사용되는 DTO
 * @since 2025.04.28
 * @author 윤영찬
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupRequestDTO {

    @Email(message = "유효한 이메일을 입력해 주세요.")
    @NotBlank(message = "이메일은 필수 항목입니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 항목입니다.")
    private String password;

    @NotBlank(message = "성별은 필수 항목입니다.")
    private String sex;

    @NotBlank(message = "이름은 필수 항목입니다.")
    private String name;

    @NotNull(message = "생년월일은 필수 항목입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @NotNull(message = "소비성향은 필수 항목입니다.")
    private ConsumptionType consumptionType;

    @NotBlank(message = "소비 목표는 필수 항목입니다.")
    private String consumptionGoal;

}
