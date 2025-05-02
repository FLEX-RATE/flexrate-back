package com.flexrate.flexrate_back.loan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class LoanReviewApplicationResponse {
    private String name;
    private String screeningDate;
    private Integer loanLimit;
    private Float initialRate;
    private Float rateRangeFrom;
    private Float rateRangeTo;
}
