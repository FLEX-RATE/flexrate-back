package com.flexrate.flexrate_back.loan.application.repository;

import com.flexrate.flexrate_back.loan.domain.LoanTransaction;
import com.flexrate.flexrate_back.loan.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LoanTransactionRepository extends JpaRepository<LoanTransaction, Long> {
    List<LoanTransaction> findByMember_MemberIdAndTypeAndOccurredAtBetween(Long memberId, TransactionType type, LocalDateTime startDate, LocalDateTime endDate);
    Optional<LoanTransaction> findFirstByMember_MemberIdAndTypeOrderByOccurredAtDesc(Long memberId, TransactionType type);
}
