package com.flexrate.flexrate_back.member.domain.repository;

import com.flexrate.flexrate_back.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * // 이메일로 회원 찾기 (회원가입 시 이메일 중복 체크용)
 * @since 2025.04.28
 * @author 윤영찬
 */


public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
}

