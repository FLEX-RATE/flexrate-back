package com.flexrate.flexrate_back.loan.application.repository;

import com.flexrate.flexrate_back.loan.domain.LoanTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoanTransactionQueryRepository {
    Page<LoanTransaction> findByMemberId(Long memberId, Pageable pageable);
}
