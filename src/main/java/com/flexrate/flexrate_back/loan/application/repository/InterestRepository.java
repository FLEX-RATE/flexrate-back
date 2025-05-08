package com.flexrate.flexrate_back.loan.application.repository;

import com.flexrate.flexrate_back.loan.domain.Interest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterestRepository extends JpaRepository<Interest, Long> {

    List<Interest> findTop2ByLoanApplication_ApplicationIdOrderByInterestDateDesc(Long applicationId);
    List<Interest> findByLoanApplication_ApplicationId(Long applicationId);

}
