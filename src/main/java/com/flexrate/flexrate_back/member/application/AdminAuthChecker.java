package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.auth.resolver.CurrentMemberId;

public interface AdminAuthChecker {
    boolean isAdmin(@CurrentMemberId Long memberId);
}
