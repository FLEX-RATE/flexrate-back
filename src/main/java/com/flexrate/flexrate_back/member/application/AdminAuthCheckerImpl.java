package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.auth.domain.jwt.JwtTokenProvider;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class AdminAuthCheckerImpl implements AdminAuthChecker {
    private final MemberService memberService;

    @Override
    public boolean isAdmin(Principal principal) {
        Member member = memberService.findById(Long.parseLong(principal.getName()));
        return Role.ADMIN.equals(member.getRole());
    }
}
