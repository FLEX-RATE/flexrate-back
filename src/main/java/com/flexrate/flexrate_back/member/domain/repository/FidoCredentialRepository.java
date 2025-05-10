package com.flexrate.flexrate_back.member.domain.repository;

import com.flexrate.flexrate_back.auth.domain.FidoCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/*
* 패스키 등록 여부를 확인
* @since 2025.05.06
* @author 윤영찬
* */

@Repository
public interface FidoCredentialRepository extends JpaRepository<FidoCredential, Long> {
    Optional<FidoCredential> findByMember_MemberId(Long memberId);
    boolean existsByPublicKey(String publicKey);

    boolean existsByCredentialId(Long credential);
}