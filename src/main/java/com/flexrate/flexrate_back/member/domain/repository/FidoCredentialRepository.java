package com.flexrate.flexrate_back.member.domain.repository;

import com.flexrate.flexrate_back.auth.domain.FidoCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/*
 * FIDO 자격 증명을 저장, 조회하는 JPA 레포지토리.
 * @param <FidoCredential> FIDO 자격 증명 엔터티
 * @param <Long> 자격 증명의 기본 키 타입
 * @return 저장된 FIDO 자격 증명 및 관련 조회 결과
 * @since 2025.05.06
 * @author 윤영찬
* */

@Repository
public interface FidoCredentialRepository extends JpaRepository<FidoCredential, Long> {
    Optional<FidoCredential> findByMember_MemberId(Long memberId);
    boolean existsByPublicKey(String publicKey);

    boolean existsByCredentialId(Long credential);
}