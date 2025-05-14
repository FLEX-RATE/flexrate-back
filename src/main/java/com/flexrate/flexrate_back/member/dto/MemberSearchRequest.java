package com.flexrate.flexrate_back.member.dto;

import com.flexrate.flexrate_back.member.enums.MemberStatus;
import com.flexrate.flexrate_back.member.enums.Sex;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record MemberSearchRequest(
        @Size(max = 10, message = "이름은 최대 10자까지 입력할 수 있습니다.")
        String name,

        @Size(max = 50, message = "이메일은 최대 50자까지 입력할 수 있습니다.")
        String email,

        Sex sex,
        LocalDate birthDateStart,
        LocalDate birthDateEnd,

        MemberStatus memberStatus,
        LocalDate startDate,
        LocalDate endDate,

        Boolean hasLoan,

        @Min(value = 0, message = "거래 내역 횟수는 0 이상이어야 합니다.")
        Integer transactionCountMin,
        Integer transactionCountMax,

        @Min(value = 0, message = "페이지는 0 이상이어야 합니다.")
        Integer page,

        @Min(value = 1, message = "사이즈는 1 이상이어야 합니다.")
        @Max(value = 200, message = "사이즈는 200 이하만 가능합니다.")
        Integer size,

        SortBy sortBy
) {
    public enum SortBy {
        ID, NAME, CREATED_AT
    }
}