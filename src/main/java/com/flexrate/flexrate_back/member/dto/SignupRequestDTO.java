package com.flexrate.flexrate_back.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.flexrate.flexrate_back.member.enums.ConsumeGoal;
import com.flexrate.flexrate_back.member.enums.ConsumptionType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

/*
 * 회원 가입 시 입력받는 사용자 검증 전달 DTO
 * @since 2025.05.02
 * @author 윤영찬
 * */

@Builder
public record SignupRequestDTO(

        @Email(message = "유효한 이메일을 입력해 주세요.")
        @NotBlank(message = "이메일은 필수 항목입니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수 항목입니다.")
        String password,

        @NotBlank(message = "성별은 필수 항목입니다.")
        String sex,

        @NotBlank(message = "이름은 필수 항목입니다.")
        String name,

        @NotNull(message = "생년월일은 필수 항목입니다.")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate birthDate,

        @NotNull(message = "소비성향은 필수 항목입니다.")
        @JsonProperty("consumption_type")
        ConsumptionType consumptionType,

        @NotNull(message = "소비 목표는 필수 항목입니다.")
        @JsonProperty("consume_goal")
        ConsumeGoal consumeGoal,

        @JsonProperty("passkeys")
        List<PasskeyRequestDTO> passkeys
) {}
