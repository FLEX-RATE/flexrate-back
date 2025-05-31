package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.auth.resolver.CurrentMemberId;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAuthCheckerImpl implements AdminAuthChecker {
    private final MemberService memberService;

    @Override
    public boolean isAdmin(@CurrentMemberId Long memberId) {
        Member member = memberService.findById(memberId);

        if (Role.ADMIN.equals(member.getRole())) {
            log.info("관리자 권한 확인 성공");
            return true;
        } else {
            log.warn("관리자 권한 확인 실패:\nmemberRole = {}", member.getRole());
            return false;
        }
    }
}
