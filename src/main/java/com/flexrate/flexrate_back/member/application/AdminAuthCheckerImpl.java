package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAuthCheckerImpl implements AdminAuthChecker {
    private final MemberService memberService;

    @Override
    public boolean isAdmin(Principal principal) {
        Member member = memberService.findById(Long.parseLong(principal.getName()));
        if (Role.ADMIN.equals(member.getRole())) {
            log.info("관리자 권한 확인 성공");
            return true;
        } else {
            log.warn("관리자 권한 확인 실패: memberRole = {}", member.getRole());
            return false;
        }
    }
}
