package com.flexrate.flexrate_back.member.enums;

public enum Role {
    ADMIN,
    MEMBER;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}