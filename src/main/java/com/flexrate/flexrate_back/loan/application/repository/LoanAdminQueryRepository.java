package com.flexrate.flexrate_back.loan.application.repository;

import com.flexrate.flexrate_back.loan.domain.LoanApplication;
import com.flexrate.flexrate_back.loan.dto.LoanAdminSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanAdminQueryRepository {
    Page<LoanApplication> searchLoans(LoanAdminSearchRequest request, Pageable pageable);
}