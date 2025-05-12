package com.flexrate.flexrate_back.member.domain.repository;

import com.flexrate.flexrate_back.auth.domain.MfaLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
* MFA 인증 로그를 저장, 조회하는 JPA 레포지토리
* @param <MfaLog> MFA 인증 로그 엔터티
* @param <Long> MFA 로그의 기본 키 타입
* @since 2025.05.10
* @author 윤영찬
* */

@Repository
public interface MfaLogRepository extends JpaRepository<MfaLog, Long> {
}
