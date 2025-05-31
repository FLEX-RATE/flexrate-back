package com.flexrate.flexrate_back.auth.domain.repository;

import com.flexrate.flexrate_back.auth.domain.PinCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PinCredentialRepository extends JpaRepository<PinCredential, Long> {
    Optional<PinCredential> findByMember_MemberId(Long memberId);
}