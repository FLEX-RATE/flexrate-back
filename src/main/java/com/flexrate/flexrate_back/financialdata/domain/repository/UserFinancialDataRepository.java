package com.flexrate.flexrate_back.financialdata.domain.repository;

import com.flexrate.flexrate_back.financialdata.domain.UserFinancialData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFinancialDataRepository extends JpaRepository<UserFinancialData, Long>, UserFinancialDataQueryRepository {}
