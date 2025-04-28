package com.flexrate.flexrate_back.member.domain.repository;

import com.flexrate.flexrate_back.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


/*
 * 이메일을 기준으로 회원 존재 여부를 확인
 * @param email 조회할 이메일
 * @return 해당 이메일로 등록된 회원이 존재하면 true, 없으면 false
 * @since 2025.04.28
 * @author 윤영찬
 */

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);  // 이메일 중복 검사
}

