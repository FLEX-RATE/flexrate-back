package com.flexrate.flexrate_back.loan.application.repository;

import com.flexrate.flexrate_back.loan.domain.LoanApplication;
import com.flexrate.flexrate_back.loan.domain.LoanProduct;
import com.flexrate.flexrate_back.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanProductRepository extends JpaRepository<LoanProduct, Long> {
}
