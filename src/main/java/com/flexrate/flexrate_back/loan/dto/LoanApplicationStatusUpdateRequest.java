package com.flexrate.flexrate_back.loan.dto;

import com.flexrate.flexrate_back.loan.enums.LoanApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record LoanApplicationStatusUpdateRequest(
        @NotNull(message = "대출 상태는 필수입니다.(PENDING, REJECTED, EXECUTED, COMPLETED)")
        LoanApplicationStatus status,

        String reason
) {}
