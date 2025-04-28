package com.flexrate.flexrate_back.member.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class SignupDTO {
    private String email;
    private String password;
    private String sex;
    private String name;
    private LocalDate birthDate;
    private String consumptionType;
    private String consumptionGoal;
//    private List<PasskeyDTO> passkeys; 필수 X
    private List<ConsentDTO> consents;
}
