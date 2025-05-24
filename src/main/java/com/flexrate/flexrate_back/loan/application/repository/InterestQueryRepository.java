package com.flexrate.flexrate_back.loan.application.repository;

import com.flexrate.flexrate_back.loan.domain.LoanApplication;

import java.time.LocalDate;

public interface InterestQueryRepository {
    int countByConditionsWithInterestChangedTrue(LoanApplication app, LocalDate startDate, LocalDate endDate);
}
