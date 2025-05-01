package com.flexrate.flexrate_back.member.dto;

import com.flexrate.flexrate_back.member.enums.MemberStatus;
import com.flexrate.flexrate_back.member.enums.Sex;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record PatchMemberRequest(
        @Size(max = 20, message = "이름은 최대 20자까지 입력할 수 있습니다.")
        String name,

        Sex sex,
        LocalDate birthDate,
        MemberStatus memberStatus
) {
}
