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
    private String sex; // 성별 (문자열로 받아서 Enum으로 변환)
    private String name;
    private LocalDate birthDate;
    private String consumptionType;
    private String consumptionGoal;
    private List<PasskeyDTO> passkeys; // FIDO2 패스키 정보
    private List<ConsentDTO> consents; // 약관 동의 정보
}
