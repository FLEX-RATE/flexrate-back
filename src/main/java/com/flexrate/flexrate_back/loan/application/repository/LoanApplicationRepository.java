package com.flexrate.flexrate_back.loan.application.repository;

import com.flexrate.flexrate_back.loan.domain.LoanApplication;
import com.flexrate.flexrate_back.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    List<LoanApplication> member(Member member);
    List<LoanApplication> findByEndDateBetween(LocalDateTime start, LocalDateTime end);
    Optional<LoanApplication> findByMember(Member member);

    @Query(value = """
    SELECT 
      ROUND(100 - ((SUM(CASE WHEN credit_score < :score THEN 1 ELSE 0 END) * 100.0) / COUNT(*)))
    FROM loan_application
    """, nativeQuery = true)
    int findCreditScorePercentile(@Param("score") int score);

}
