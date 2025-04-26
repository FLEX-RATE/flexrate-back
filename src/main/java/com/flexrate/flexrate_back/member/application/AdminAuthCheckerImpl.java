package com.flexrate.flexrate_back.member.application;

import org.springframework.stereotype.Component;

@Component
public class AdminAuthCheckerImpl implements AdminAuthChecker {

    // 추후 변경
    private static final String ADMIN_TOKEN = "adminToken";

    @Override
    public boolean isAdmin(String adminToken) {
        return ADMIN_TOKEN.equals(adminToken);
    }
}
