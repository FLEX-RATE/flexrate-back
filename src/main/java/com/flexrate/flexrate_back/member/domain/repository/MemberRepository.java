package com.flexrate.flexrate_back.member.domain.repository;

import com.flexrate.flexrate_back.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);
    Optional<Member> findByEmail(String email);

    @Query("SELECT DISTINCT m FROM Member m JOIN FETCH m.loanApplication a JOIN FETCH a.product")
    List<Member> findAllWithLoanApplication();

}