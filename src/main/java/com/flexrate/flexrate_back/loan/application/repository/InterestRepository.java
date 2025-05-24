package com.flexrate.flexrate_back.loan.application.repository;

import com.flexrate.flexrate_back.loan.domain.Interest;
import com.flexrate.flexrate_back.loan.domain.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InterestRepository extends JpaRepository<Interest, Long> {

    List<Interest> findTop2ByLoanApplication_ApplicationIdOrderByInterestDateDesc(Long applicationId);
    List<Interest> findByLoanApplication_ApplicationId(Long applicationId);
    Optional<Interest> findByInterestDateAndLoanApplication(LocalDate interestDate, LoanApplication LoanApplication);

}
