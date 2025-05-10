package com.flexrate.flexrate_back.member.domain.repository;

import com.flexrate.flexrate_back.financialdata.domain.UserFinancialData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFinancialDataRepository extends JpaRepository<UserFinancialData, Long> {
}

