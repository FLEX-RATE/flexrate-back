package com.flexrate.flexrate_back.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.flexrate.flexrate_back.member.enums.ConsumeGoal;
import com.flexrate.flexrate_back.member.enums.ConsumptionType;
import com.flexrate.flexrate_back.member.enums.Sex;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record SignupPasswordRequestDTO(
        @Email(message = "유효한 이메일을 입력해 주세요.")
        @NotBlank(message = "이메일은 필수 항목입니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수 항목입니다.")
        String password,

        @NotNull(message = "성별은 필수 항목입니다.")
        Sex sex,

        @NotBlank(message = "이름은 필수 항목입니다.")
        String name,

        @NotNull(message = "생년월일은 필수 항목입니다.")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate birthDate,

        @NotNull(message = "소비성향은 필수 항목입니다.")
        ConsumptionType consumptionType,

        @NotNull(message = "소비 목표는 필수 항목입니다.")
        ConsumeGoal consumeGoal,

        @NotBlank(message = "pin은 필수 항목입니다.")
        @Pattern(regexp = "\\d{6}", message = "PIN은 6자리 숫자여야 합니다.")
        String pin
) {}
