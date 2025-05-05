package com.flexrate.flexrate_back.member.domain.repository;

import com.flexrate.flexrate_back.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // 기본 CRUD 메서드 제공
    Optional<Member> findByName(String name);

    Optional<Member> findByEmail(String email);
}