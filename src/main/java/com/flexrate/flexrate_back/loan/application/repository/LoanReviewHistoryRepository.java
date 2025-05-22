package com.flexrate.flexrate_back.loan.application.repository;

import com.flexrate.flexrate_back.loan.domain.LoanReviewHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanReviewHistoryRepository extends JpaRepository<LoanReviewHistory, Long> {
}
