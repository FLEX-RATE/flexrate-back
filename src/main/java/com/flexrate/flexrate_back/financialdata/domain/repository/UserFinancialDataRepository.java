package com.flexrate.flexrate_back.financialdata.domain.repository;

import com.flexrate.flexrate_back.financialdata.domain.UserFinancialData;
import com.flexrate.flexrate_back.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserFinancialDataRepository extends JpaRepository<UserFinancialData, Long>, UserFinancialDataQueryRepository {
    List<UserFinancialData> findAllByMember(Member member);
}
