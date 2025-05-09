package com.flexrate.flexrate_back.loan.dto;

import com.flexrate.flexrate_back.loan.enums.LoanApplicationStatus;
import com.flexrate.flexrate_back.loan.enums.LoanType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record LoanAdminSearchRequest(
        @Min(value = 0, message = "페이지는 0 이상이어야 합니다.")
        Integer page,

        @Min(value = 1, message = "사이즈는 1 이상이어야 합니다.")
        @Max(value = 200, message = "사이즈는 200 이하만 가능합니다.")
        Integer size,

        List<LoanApplicationStatus> status,

        @Size(max = 10, message = "신청자 이름은 최대 10자까지 입력할 수 있습니다.")
        String applicant,

        Long applicantId,

        LocalDate appliedFrom,
        LocalDate appliedTo,

        Integer limitFrom,
        Integer limitTo,

        Float rateFrom,
        Float rateTo,

        Integer prevLoanCountFrom,
        Integer prevLoanCountTo,

        LoanType type
) {
}
