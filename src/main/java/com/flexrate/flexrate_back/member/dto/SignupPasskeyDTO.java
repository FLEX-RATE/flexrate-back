package com.flexrate.flexrate_back.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.flexrate.flexrate_back.member.enums.ConsumeGoal;
import com.flexrate.flexrate_back.member.enums.ConsumptionType;
import com.flexrate.flexrate_back.member.enums.Sex;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record SignupPasskeyDTO(
        @Email
        String email,

        @NotNull
        Sex sex,

        @NotBlank
        String name,

        @NotNull @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate birthDate,

        @NotNull @JsonProperty("consumption_type")
        ConsumptionType consumptionType,

        @NotNull @JsonProperty("consume_goal")
        ConsumeGoal consumeGoal,

        @JsonProperty("passkeys")
        List<PasskeyRequestDTO> passkeys,

        @JsonProperty("consents") List<ConsentRequestDTO> consents

) {}