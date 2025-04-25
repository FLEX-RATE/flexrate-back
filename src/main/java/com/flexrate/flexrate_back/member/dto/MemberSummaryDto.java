package com.flexrate.flexrate_back.member.dto;

import com.flexrate.flexrate_back.member.enums.MemberStatus;
import com.flexrate.flexrate_back.member.enums.Sex;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record MemberSummaryDto(
        Long id,
        String name,
        String email,
        Sex sex,
        LocalDate birthDate,
        MemberStatus memberStatus,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt,
        Boolean hasLoan,
        Integer loanCount
) {}
