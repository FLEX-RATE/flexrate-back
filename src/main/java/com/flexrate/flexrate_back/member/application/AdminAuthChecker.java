package com.flexrate.flexrate_back.member.application;

public interface AdminAuthChecker {
    boolean isAdmin(String adminToken);
}
