package com.flexrate.flexrate_back.member.domain.repository;

import com.flexrate.flexrate_back.auth.domain.MfaLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MfaLogRepository extends JpaRepository<MfaLog, Long> {
}
