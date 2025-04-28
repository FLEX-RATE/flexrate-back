package com.flexrate.flexrate_back.member.dto;

import com.flexrate.flexrate_back.member.enums.ConsumptionType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/*
 * 회원가입 요청 시 사용되는 DTO
 * @since 2025.04.28
 * @author 윤영찬
 */

@Getter
@Builder
public class SignupDTO {
    private String email;
    private String password;
    private String sex;
    private String name;
    private LocalDate birthDate;
    private ConsumptionType consumptionType;
    private String consumptionGoal;
    //    private List<PasskeyDTO> passkeys; 필수 X
//    private List<ConsentDTO> consents;
}
