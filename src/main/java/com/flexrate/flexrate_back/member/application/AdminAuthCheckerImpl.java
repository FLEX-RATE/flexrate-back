package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.auth.domain.jwt.JwtTokenProvider;
import com.flexrate.flexrate_back.member.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminAuthCheckerImpl implements AdminAuthChecker {
    private final JwtTokenProvider tokenProvider;

    @Override
    public boolean isAdmin(String token) {
        if (token == null || token.isEmpty()) return false;

        // Bearer 접두어 제거
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 토큰 유효성 검사
        if (!tokenProvider.validToken(token)) {
            return false;
        }

        // Role 추출 및 검사
        String role = tokenProvider.getRole(token);
        return Role.ADMIN.name().equals(role);
    }
}
